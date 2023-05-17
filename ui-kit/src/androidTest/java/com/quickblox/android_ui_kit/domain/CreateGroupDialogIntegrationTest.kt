/*
 * Created by Injoit on 25.1.2023.
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
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.users.model.QBUser
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
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
class CreateGroupDialogIntegrationTest : BaseTest() {
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
    fun correctDialogName_execute_receiveDialogEntity() = runBlocking {
        withContext(Dispatchers.Main) {
            runCatching {
                CreateGroupDialogUseCase(generateDialogName()).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NoException, actual: Exception, details $error")
            }
        }

        assertTrue(createdDialog?.getDialogId()!!.isNotEmpty())
        assertTrue(createdDialog?.getParticipantIds()!!.contains(USER_ID))
        assertTrue(createdDialog?.getUpdatedAt()!!.isNotEmpty())
        assertTrue(createdDialog?.getName()!!.isNotEmpty())
        assertEquals(createdDialog?.getType()!!, DialogEntity.Types.GROUP)
    }

    @Test
    fun emptyDialogName_execute_receiveError() = runBlocking {
        val errorLatch = CountDownLatch(1)
        withContext(Dispatchers.IO) {
            runCatching {
                CreateGroupDialogUseCase("").execute()
            }.onSuccess { result ->
                fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        errorLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, errorLatch.count)
    }

    @Test
    fun correctDialogNameAndParticipants_execute_receiveDialogEntity() = runBlocking {
        val receivedCreatedDialogEventLatch = CountDownLatch(1)
        subscribeToReceiveCreatedDialogEventInSystemMessageListener(receivedCreatedDialogEventLatch)

        withContext(Dispatchers.IO) {
            runCatching {
                CreateGroupDialogUseCase(generateDialogName(), arrayListOf(USER_ID, OPPONENT_ID)).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        receivedCreatedDialogEventLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, receivedCreatedDialogEventLatch.count)

        assertTrue(createdDialog?.getDialogId()!!.isNotEmpty())
        assertTrue(createdDialog?.getUpdatedAt()!!.isNotEmpty())
        assertTrue(createdDialog?.getName()!!.isNotEmpty())
        assertEquals(createdDialog?.getType()!!, DialogEntity.Types.GROUP)

        assertTrue(createdDialog?.getParticipantIds()!!.contains(USER_ID))
        assertTrue(createdDialog?.getParticipantIds()!!.contains(USER_OPPONENT_ID_1))
    }

    private fun subscribeToReceiveCreatedDialogEventInSystemMessageListener(receivedMessageLatch: CountDownLatch) {
        opponentChatService?.systemMessagesManager?.addSystemMessageListener(object : QBSystemMessageListener {
            override fun processMessage(qbChatMessage: QBChatMessage) {
                if (EventMessageParser.isCreatedDialogEventFrom(qbChatMessage)) {
                    receivedMessageLatch.countDown()
                }
            }

            override fun processError(exception: QBChatException?, qbChatMessage: QBChatMessage?) {}
        })
    }

    private fun generateDialogName(): String {
        return "${System.currentTimeMillis()}_group_dialog_from_ui_kit"
    }
}