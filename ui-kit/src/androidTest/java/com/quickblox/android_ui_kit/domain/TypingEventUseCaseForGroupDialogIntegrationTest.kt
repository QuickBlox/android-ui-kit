/*
 * Created by Injoit on 4.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.*
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.TypingEventUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Constructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TypingEventUseCaseForGroupDialogIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var opponentChatService: QBChatService? = null
    private var connectionUseCase: ConnectionUseCase? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()

        connectionUseCase = ConnectionUseCase()
        connectionUseCase?.execute()

        createdDialog = CreateGroupDialogUseCase("${System.currentTimeMillis()}", arrayListOf(OPPONENT_ID)).execute()

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
    fun release() = runBlocking {
        connectionUseCase?.release()

        deleteDialog(createdDialog?.getDialogId())

        logoutFromRest()
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun createDialog_sendStartAndStopTyping_receivedStartAndStopTyping() = runBlocking {
        val startTypingReceivedLatch = CountDownLatch(1)
        val stopTypingReceivedLatch = CountDownLatch(1)

        val job = CoroutineScope(Dispatchers.IO).launch {
            val useCase = TypingEventUseCase(createdDialog!!)
            useCase.timerLength = 10000
            useCase.execute().collect { typingEntity ->
                if (typingEntity?.isStarted()!!) {
                    startTypingReceivedLatch.countDown()
                }
                if (typingEntity.isStopped()) {
                    stopTypingReceivedLatch.countDown()
                }
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        sendStartedTypingFromOpponent(createdDialog!!, opponentChatService!!)

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        sendStoppedTypingFromOpponent(createdDialog!!, opponentChatService!!)

        startTypingReceivedLatch.await(3, TimeUnit.SECONDS)
        stopTypingReceivedLatch.await(3, TimeUnit.SECONDS)

        job.cancel()

        assertEquals(0, startTypingReceivedLatch.count)
        assertEquals(0, stopTypingReceivedLatch.count)
    }

    private fun sendStartedTypingFromOpponent(dialogEntity: DialogEntity, chatService: QBChatService) {
        val qbChatDialog = buildQBDialog(dialogEntity, chatService)
        qbChatDialog.sendIsTypingNotification()
    }

    private fun sendStoppedTypingFromOpponent(dialogEntity: DialogEntity, chatService: QBChatService) {
        val qbChatDialog = buildQBDialog(dialogEntity, chatService)
        qbChatDialog.sendStopTypingNotification()
    }

    private fun buildQBDialog(dialogEntity: DialogEntity, chatService: QBChatService): QBChatDialog {
        val qbChatDialog = QBChatDialog()
        qbChatDialog.type = QBDialogType.GROUP
        qbChatDialog.setOccupantsIds(dialogEntity.getParticipantIds()?.toMutableList())
        qbChatDialog.dialogId = dialogEntity.getDialogId()
        qbChatDialog.name = dialogEntity.getName()
        qbChatDialog.initForChat(chatService)
        qbChatDialog.join(null)

        return qbChatDialog
    }
}