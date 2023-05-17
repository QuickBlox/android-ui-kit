/*
 * Created by Injoit on 10.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.*
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.*
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.listeners.QBMessageStatusListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
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
class ReadMessagesUseCaseIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var loadedMessage: IncomingChatMessageEntity? = null
    private var connectionUseCase: ConnectionUseCase? = null
    private var opponentChatService: QBChatService? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()

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

        logoutFromChat()

        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createGroupDialog_sendChatMessageAndSendRead_receivedReadMessage() = runBlocking {
        createGroupDialog()

        deleteAllMessagesIn(createdDialog!!)

        val receivedMessageLatch = CountDownLatch(1)

        CoroutineScope(Dispatchers.IO).launch {
            MessagesEventUseCase(createdDialog!!).execute().collect { messageEntity ->
                messageEntity?.let {
                    if (messageEntity is IncomingChatMessageEntity) {
                        loadedMessage = messageEntity
                        receivedMessageLatch.countDown()
                    }
                }
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        val opponentUser = buildOpponentUser()
        val qbDialog = buildOpponentGroupQBChatDialogFrom(createdDialog!!, opponentChatService!!, opponentUser)
        sendChatMessageToDialog(qbDialog)

        receivedMessageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, receivedMessageLatch.count)

        val readMessageLatch = CountDownLatch(1)
        opponentChatService?.messageStatusesManager?.addMessageStatusListener(StatusMessageListenerImpl(readMessageLatch))

        ReadMessageUseCase(loadedMessage!!).execute()

        readMessageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, readMessageLatch.count)
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

    private fun deleteAllMessagesIn(dialog: DialogEntity?) {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog?.getDialogId()!!

        val loadedMessages = QBRestChatService.getDialogMessages(qbDialog, null).perform()

        val needToDeleteMessageIds = arrayListOf<String>()
        loadedMessages.forEach { qbChatMessage ->
            needToDeleteMessageIds.add(qbChatMessage.id)
        }

        if (needToDeleteMessageIds.isNotEmpty()) {
            QBRestChatService.deleteMessages(needToDeleteMessageIds.toSet(), true).perform()
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

    @Test
    @ExperimentalCoroutinesApi
    fun createPrivateDialog_sendChatMessageAndSendRead_receivedReadMessage() = runBlocking {
        createPrivateDialog()

        deleteAllMessagesIn(createdDialog!!)

        val receivedMessageLatch = CountDownLatch(1)

        CoroutineScope(Dispatchers.IO).launch {
            MessagesEventUseCase(createdDialog!!).execute().collect { messageEntity ->
                messageEntity?.let {
                    if (messageEntity is IncomingChatMessageEntity) {
                        loadedMessage = messageEntity
                        receivedMessageLatch.countDown()
                    }
                }
            }
        }

        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        val opponentUser = buildOpponentUser()
        val qbDialog = buildOpponentPrivateQBChatDialogFrom(createdDialog!!, opponentChatService!!, opponentUser)
        sendChatMessageToDialog(qbDialog)

        receivedMessageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, receivedMessageLatch.count)

        val readMessageLatch = CountDownLatch(1)
        opponentChatService?.messageStatusesManager?.addMessageStatusListener(StatusMessageListenerImpl(readMessageLatch))

        ReadMessageUseCase(loadedMessage!!).execute()

        readMessageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, readMessageLatch.count)
    }

    private suspend fun createPrivateDialog() {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(OPPONENT_ID).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception")
            }
        }
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

    private fun sendChatMessageToDialog(qbDialog: QBChatDialog) {
        val qbMessage = QBChatMessage()
        qbMessage.body = "test_message: ${System.currentTimeMillis()}"
        qbMessage.setSaveToHistory(true)

        qbDialog.sendMessage(qbMessage)
    }

    private inner class StatusMessageListenerImpl(private val messageReadLatch: CountDownLatch) :
        QBMessageStatusListener {
        override fun processMessageDelivered(p0: String?, p1: String?, p2: Int?) {
            fail("expected: Read, actual: Delivered")
        }

        override fun processMessageRead(messageId: String, dialogId: String, senderId: Int) {
            assertEquals(loadedMessage!!.geMessageId(), messageId)
            messageReadLatch.countDown()
        }
    }
}