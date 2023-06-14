/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.remote

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.OPPONENT_ID
import com.quickblox.android_ui_kit.OPPONENT_LOGIN
import com.quickblox.android_ui_kit.OPPONENT_PASSWORD
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Constructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TypingEventInPrivateDialogIntegrationTest : BaseTest() {
    private val remoteDataSource: RemoteDataSource = RemoteDataSourceImpl()
    private var createdDialog: RemoteDialogDTO? = null
    private var opponentChatService: QBChatService? = null

    @Before
    fun init() {
        initQuickblox()
        loginToRest()

        remoteDataSource.connect()

        createdDialog = createPrivateDialog()
        opponentChatService = createOpponentChatServiceAndLogin()
    }

    private fun createPrivateDialog(): RemoteDialogDTO {
        val dialogDTO = RemoteDialogDTO()
        dialogDTO.type = QBDialogType.PRIVATE.code
        dialogDTO.participantIds = mutableListOf(OPPONENT_ID)

        val createdDialog = remoteDataSource.createDialog(dialogDTO)
        return createdDialog
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
        deleteDialog(createdDialog?.id)

        remoteDataSource.disconnect()

        logoutFromRest()
    }

    @Test
    fun createDialog_sendStartAndStopTyping_receivedStartAndStopTyping() = runBlocking {
        val typingReceivedLatch = CountDownLatch(2)

        val syncDialogsScope = launch(Dispatchers.IO) {
            remoteDataSource.subscribeTypingEvent().collect { typingDTO ->
                typingReceivedLatch.countDown()
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        sendStartedTypingFromOpponent(createdDialog!!, opponentChatService!!)

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        sendStoppedTypingFromOpponent(createdDialog!!, opponentChatService!!)

        typingReceivedLatch.await(3, TimeUnit.SECONDS)

        syncDialogsScope.cancel()

        assertEquals(0, typingReceivedLatch.count)
    }

    private fun sendStartedTypingFromOpponent(dialogDTO: RemoteDialogDTO, chatService: QBChatService) {
        val qbChatDialog = buildQBDialog(dialogDTO, chatService)
        qbChatDialog.sendIsTypingNotification()
    }

    private fun sendStoppedTypingFromOpponent(dialogDTO: RemoteDialogDTO, chatService: QBChatService) {
        val qbChatDialog = buildQBDialog(dialogDTO, chatService)
        qbChatDialog.sendStopTypingNotification()
    }

    private fun buildQBDialog(dialogDTO: RemoteDialogDTO, chatService: QBChatService): QBChatDialog {
        val qbChatDialog = QBChatDialog()
        qbChatDialog.type = QBDialogType.PRIVATE
        qbChatDialog.setOccupantsIds(dialogDTO.participantIds?.toMutableList())
        qbChatDialog.dialogId = dialogDTO.id
        qbChatDialog.initForChat(chatService)

        return qbChatDialog
    }
}