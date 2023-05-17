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
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.MessagesEventUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert
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

@Ignore("need to fix coroutine/flow behaviour")
@RunWith(AndroidJUnit4::class)
class MessagesEventUseCaseWithReadStatusIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var connectionUseCase: ConnectionUseCase? = null
    private var opponentChatService: QBChatService? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()

        initConnectionUseCase()

        opponentChatService = createOpponentChatServiceAndLogin()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    private suspend fun initConnectionUseCase() {
        if (connectionUseCase == null) {
            connectionUseCase = ConnectionUseCase()
        }
        connectionUseCase?.execute()
    }

    private fun createOpponentChatServiceAndLogin(): QBChatService {
        val constructor: Constructor<QBChatService> = QBChatService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val qbChatService = constructor.newInstance()

        return qbChatService
    }


    @After
    fun release() = runBlocking {
        deleteDialog(createdDialog)

        connectionUseCase?.release()

        opponentChatService?.destroy()

        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createGroupDialog_sendChatMessageAndSendRead_receivedReadMessage() = runBlocking {
        createGroupDialog()

        val receivedMessageLatch = CountDownLatch(1)

        var loadedMessage: OutgoingChatMessageEntity? = null
        val messagesEventJob = CoroutineScope(Dispatchers.IO).launch {
            MessagesEventUseCase(createdDialog!!).execute().collect { messageEntity ->
                messageEntity?.let {
                    loadedMessage = messageEntity as OutgoingChatMessageEntity
                    receivedMessageLatch.countDown()
                }
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        val sentQBChatMessage = sendChatMessageToGroupDialog(createdDialog!!, QBChatService.getInstance())

        sendReadStatusToGroupDialog(sentQBChatMessage, createdDialog!!, opponentChatService!!)

        receivedMessageLatch.await(5, TimeUnit.SECONDS)

        messagesEventJob.cancel()

        assertEquals(0, receivedMessageLatch.count)
        assertEquals(OutgoingChatMessageEntity.OutgoingStates.READ, loadedMessage!!.getOutgoingState())
        assertEquals(sentQBChatMessage.id, loadedMessage!!.geMessageId())
    }

    private suspend fun createGroupDialog() {
        withContext(Dispatchers.Main) {
            runCatching {
                val name = "group_dialog_test: ${System.currentTimeMillis()}"
                val opponents = arrayListOf(USER_ID, OPPONENT_ID)
                CreateGroupDialogUseCase(name, opponents).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception")
            }
        }
    }

    private fun buildOpponentGroupQBChatDialogFrom(
        dialog: DialogEntity,
        chatService: QBChatService,
        opponentUser: QBUser
    ): QBChatDialog {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog.getDialogId()
        qbDialog.setOccupantsIds(dialog.getParticipantIds()?.toList())
        qbDialog.type = QBDialogType.GROUP

        if (!chatService.isLoggedIn) {
            chatService.login(opponentUser)
        }
        qbDialog.initForChat(chatService)
        qbDialog.join(null)

        return qbDialog
    }

    private fun sendChatMessageToGroupDialog(dialog: DialogEntity, chatService: QBChatService): QBChatMessage {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog.getDialogId()
        qbDialog.setOccupantsIds(dialog.getParticipantIds()?.toList())
        qbDialog.type = QBDialogType.GROUP

        qbDialog.initForChat(chatService)
        qbDialog.join(null)

        val qbMessage = QBChatMessage()
        qbMessage.body = "test_message: ${System.currentTimeMillis()}"
        qbMessage.setSaveToHistory(true)

        qbDialog.sendMessage(qbMessage)

        return qbMessage
    }

    private fun sendReadStatusToGroupDialog(
        message: QBChatMessage,
        groupDialog: DialogEntity,
        chatService: QBChatService
    ) {
        val qbChatDialog = buildOpponentGroupQBChatDialogFrom(groupDialog, chatService, buildOpponentUser())
        message.senderId = QBChatService.getInstance().user.id

        qbChatDialog.readMessage(message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createPrivateDialog_sendChatMessageAndSendRead_receivedReadMessage() = runBlocking {
        createPrivateDialog()

        val receivedMessageLatch = CountDownLatch(1)

        var loadedMessage: OutgoingChatMessageEntity? = null
        val messagesEventJob = CoroutineScope(Dispatchers.IO).launch {
            MessagesEventUseCase(createdDialog!!).execute().collect { messageEntity ->
                messageEntity?.let {
                    loadedMessage = messageEntity as OutgoingChatMessageEntity
                    receivedMessageLatch.countDown()
                }
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        val sentQBChatMessage = sendChatMessageToPrivateDialog(createdDialog!!, QBChatService.getInstance())

        sendReadStatusToPrivateDialog(sentQBChatMessage, createdDialog!!, opponentChatService!!)

        receivedMessageLatch.await(5, TimeUnit.SECONDS)

        messagesEventJob.cancel()

        assertEquals(0, receivedMessageLatch.count)
        assertEquals(OutgoingChatMessageEntity.OutgoingStates.READ, loadedMessage!!.getOutgoingState())
        assertEquals(sentQBChatMessage.id, loadedMessage!!.geMessageId())
    }

    private suspend fun createPrivateDialog() {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(OPPONENT_ID).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                Assert.fail("expected: Exception, actual: NotException")
            }
        }
    }

    private fun sendChatMessageToPrivateDialog(dialog: DialogEntity, chatService: QBChatService): QBChatMessage {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog.getDialogId()
        qbDialog.setOccupantsIds(dialog.getParticipantIds()?.toList())
        qbDialog.type = QBDialogType.PRIVATE

        qbDialog.initForChat(chatService)

        val qbMessage = QBChatMessage()
        qbMessage.body = "test_message: ${System.currentTimeMillis()}"
        qbMessage.setSaveToHistory(true)

        qbDialog.sendMessage(qbMessage)

        return qbMessage
    }

    private fun sendReadStatusToPrivateDialog(
        message: QBChatMessage,
        groupDialog: DialogEntity,
        chatService: QBChatService
    ) {
        val qbChatDialog = buildOpponentPrivateQBChatDialogFrom(groupDialog, chatService, buildOpponentUser())
        message.senderId = QBChatService.getInstance().user.id

        qbChatDialog.readMessage(message)
    }

    private fun buildOpponentPrivateQBChatDialogFrom(
        dialog: DialogEntity,
        chatService: QBChatService,
        opponentUser: QBUser
    ): QBChatDialog {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog.getDialogId()
        qbDialog.setOccupantsIds(dialog.getParticipantIds()?.toList())
        qbDialog.type = QBDialogType.PRIVATE

        if (!chatService.isLoggedIn) {
            chatService.login(opponentUser)
        }
        qbDialog.initForChat(chatService)

        return qbDialog
    }

    private fun buildOpponentUser(): QBUser {
        val opponentUser = QBUser()
        opponentUser.login = OPPONENT_LOGIN
        opponentUser.id = OPPONENT_ID
        opponentUser.password = OPPONENT_PASSWORD

        return opponentUser
    }
}