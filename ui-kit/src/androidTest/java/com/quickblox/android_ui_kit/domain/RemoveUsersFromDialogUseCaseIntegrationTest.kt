/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.*
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.RemoveUsersFromDialogUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.users.model.QBUser
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Constructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Ignore("need to fix coroutine/flow behaviour")
@RunWith(AndroidJUnit4::class)
class RemoveUsersFromDialogUseCaseIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var opponentChatService: QBChatService? = null

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()

        opponentChatService = createOpponentChatServiceAndLogin()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    private fun createOpponentChatServiceAndLogin(): QBChatService {
        val constructor: Constructor<QBChatService> = QBChatService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true

        val qbChatService = constructor.newInstance()

        val opponentUser = QBUser()
        opponentUser.login = OPPONENT_LOGIN
        opponentUser.id = OPPONENT_ID
        opponentUser.password = OPPONENT_PASSWORD

        try {
            qbChatService.login(opponentUser)
        } catch (exception: Exception) {
            println("exception: ${exception.message}")
        }

        return qbChatService
    }

    @After
    fun release() {
        deleteDialog(createdDialog)
        createdDialog = null

        opponentChatService?.destroy()

        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithTwoUsers_removeOneUser_receivedDialogWithOneUser() = runBlocking {
        val receivedRemoveUserToDialogEventLatch = CountDownLatch(1)
        subscribeToReceiveRemovedUserEventInSystemMessageListener(receivedRemoveUserToDialogEventLatch)

        withContext(Dispatchers.Main) {
            runCatching {
                val dialogName = "test_name ${System.currentTimeMillis()}"
                val opponentIds = arrayListOf(USER_OPPONENT_ID_2, OPPONENT_ID)
                CreateGroupDialogUseCase(dialogName, opponentIds).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        var updatedDialog: DialogEntity? = null
        withContext(Dispatchers.IO) {
            runCatching {
                RemoveUsersFromDialogUseCase(createdDialog!!, arrayListOf(USER_OPPONENT_ID_2)).execute()
            }.onSuccess { result ->
                updatedDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        receivedRemoveUserToDialogEventLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, receivedRemoveUserToDialogEventLatch.count)

        assertEquals(2, updatedDialog?.getParticipantIds()!!.size)
        assertFalse(updatedDialog?.getParticipantIds()!!.contains(USER_OPPONENT_ID_2))
    }

    private fun subscribeToReceiveRemovedUserEventInSystemMessageListener(receivedMessageLatch: CountDownLatch) {
        opponentChatService?.systemMessagesManager?.addSystemMessageListener(object : QBSystemMessageListener {
            override fun processMessage(qbChatMessage: QBChatMessage) {
                if (EventMessageParser.isRemovedUserEventFrom(qbChatMessage)) {
                    receivedMessageLatch.countDown()
                }
            }

            override fun processError(exception: QBChatException?, qbChatMessage: QBChatMessage?) {}
        })
    }
}