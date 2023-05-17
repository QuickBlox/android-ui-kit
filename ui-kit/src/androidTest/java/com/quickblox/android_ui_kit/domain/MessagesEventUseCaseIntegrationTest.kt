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
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
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
import junit.framework.Assert.assertTrue
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
class MessagesEventUseCaseIntegrationTest : BaseTest() {
    private var createdPrivateDialog: DialogEntity? = null
    private var createdGroupDialog: DialogEntity? = null
    private var connectionUseCase: ConnectionUseCase? = null
    private var messageEventUseCase: MessagesEventUseCase? = null

    private val loadedMessages = mutableListOf<MessageEntity?>()

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

    private suspend fun initMessageEventUseCase(dialogEntity: DialogEntity) {
        if (messageEventUseCase == null) {
            messageEventUseCase = MessagesEventUseCase(dialogEntity)
        }
        messageEventUseCase?.execute()
    }

    @After
    fun release() = runBlocking {
        loadedMessages.clear()

        deleteDialog(createdPrivateDialog)
        deleteDialog(createdGroupDialog)

        connectionUseCase?.release()

        opponentChatService?.destroy()

        logoutFromRest()
    }

    private fun loadMessages(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            MessagesEventUseCase(createdPrivateDialog!!).execute().collect { messageEntity ->
                messageEntity?.let {
                    loadedMessages.add(messageEntity)
                }
            }
        }
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun createPrivateAndGroupDialogs_sendFourChatMessages_receivedFourChatMessages() = runBlocking {
        createPrivateDialog()
        createGroupDialog()

        val messagesEventJob = loadMessages()

        initMessageEventUseCase(createdPrivateDialog!!)

        sendChatMessageToPrivateDialogFromOpponentWithDelay(opponentChatService!!)
        sendChatMessageToPrivateDialogFromOpponentWithDelay(opponentChatService!!)

        sendChatMessageToGroupDialogFromLoggedUserWithDelay()
        sendChatMessageToGroupDialogFromLoggedUserWithDelay()

        CountDownLatch(1).await(5, TimeUnit.SECONDS)

        messagesEventJob.cancel()

        assertEquals(4, loadedMessages.size)

        assertTrue(isAllIncomingMessagesContainsUser(loadedMessages))
        assertTrue(isAllChatMessagesContainsText(loadedMessages))
        assertTrue(isAllMessagesAreChat(loadedMessages))
    }

    private fun sendChatMessageToPrivateDialogFromOpponentWithDelay(chatService: QBChatService) {
        CountDownLatch(1).await(2, TimeUnit.SECONDS)
        sendChatMessageToPrivateDialog(chatService)
    }

    private fun sendChatMessageToPrivateDialog(chatService: QBChatService) {
        CoroutineScope(Dispatchers.IO).launch {
            val qbDialog = QBChatDialog()
            qbDialog.dialogId = createdPrivateDialog?.getDialogId()
            qbDialog.setOccupantsIds(createdPrivateDialog?.getParticipantIds()?.toList())
            qbDialog.type = QBDialogType.PRIVATE

            qbDialog.initForChat(chatService)

            val qbMessage = QBChatMessage()
            qbMessage.body = "test_message: ${System.currentTimeMillis()}"
            qbMessage.setSaveToHistory(true)

            qbDialog.sendMessage(qbMessage)
        }
    }

    private fun sendChatMessageToGroupDialogFromLoggedUserWithDelay() {
        CountDownLatch(1).await(2, TimeUnit.SECONDS)
        sendChatMessageToGroupDialog()
    }

    private fun sendChatMessageToGroupDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            val qbDialog = QBChatDialog()
            qbDialog.dialogId = createdGroupDialog?.getDialogId()
            qbDialog.setOccupantsIds(createdGroupDialog?.getParticipantIds()?.toList())
            qbDialog.type = QBDialogType.GROUP

            qbDialog.initForChat(QBChatService.getInstance())
            qbDialog.join(null)

            val qbMessage = QBChatMessage()
            qbMessage.body = "test_message: ${System.currentTimeMillis()}"
            qbMessage.setSaveToHistory(true)

            qbDialog.sendMessage(qbMessage)
        }
    }

    private fun isAllIncomingMessagesContainsUser(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        val incomingChatMessages = messages.takeWhile { message ->
            message is IncomingChatMessageEntity
        }

        run breakLoop@{
            incomingChatMessages.forEach { message ->
                val incomingMessage = message as IncomingChatMessageEntity
                if (incomingMessage.getSender() == null) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isAllChatMessagesContainsText(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        run breakLoop@{
            messages.forEach { message ->
                val incomingMessage = message as IncomingChatMessageEntity
                if (incomingMessage.getContent().isNullOrEmpty()) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isAllMessagesAreChat(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        run breakLoop@{
            messages.forEach { message ->
                val isNotChat = message !is ChatMessageEntity
                if (isNotChat) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun createOpponentChatServiceAndLogin(): QBChatService {
        val constructor: Constructor<QBChatService> = QBChatService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val qbChatService = constructor.newInstance()

        val opponentUser = QBUser()
        opponentUser.login = OPPONENT_LOGIN
        opponentUser.id = OPPONENT_ID
        opponentUser.password = OPPONENT_PASSWORD

        qbChatService.login(opponentUser)
        return qbChatService
    }

    private suspend fun createPrivateDialog() {
        withContext(Dispatchers.Main) {
            runCatching {
                CreatePrivateDialogUseCase(OPPONENT_ID).execute()
            }.onSuccess { result ->
                createdPrivateDialog = result
            }.onFailure { error ->
                Assert.fail("expected: Exception, actual: NotException")
            }
        }
    }

    private suspend fun createGroupDialog() {
        withContext(Dispatchers.Main) {
            runCatching {
                val name = "group_dialog_test: ${System.currentTimeMillis()}"
                val opponents = arrayListOf(USER_ID, OPPONENT_ID)
                CreateGroupDialogUseCase(name, opponents).execute()
            }.onSuccess { result ->
                createdGroupDialog = result
            }.onFailure { error ->
                Assert.fail("expected: Exception, actual: NotException")
            }
        }
    }
}