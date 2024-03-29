/*
 * Created by Injoit on 4.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.*
import com.quickblox.android_ui_kit.data.source.remote.parser.ForwardReplyMessageParser
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateForwardMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetAllMessagesUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendChatMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendForwardReplyMessageUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Constructor

@RunWith(AndroidJUnit4::class)
class SendForwardMessageIntegrationTest : BaseTest() {
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

        createdDialog = CreatePrivateDialogUseCase(OPPONENT_ID).execute()

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
    fun release(): Unit = runBlocking {
        connectionUseCase?.release()

        deleteDialog(createdDialog?.getDialogId())

        logoutFromRest()

        opponentChatService?.logout()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun sendFiveTextForwardedMessagesFromOpponentUser_loadMessage_messageIsForwardedAndIncoming(): Unit = runBlocking {
        val messageCount = 5

        val messageContentText = "opponent_user_forwarded_test_message_${System.currentTimeMillis()}"

        sendXMessagesByOpponent(createdDialog?.getDialogId()!!, messageContentText, messageCount)
        val loadedMessages = loadXChatMessages(messagesCount = messageCount)

        sendForwardedMessageByOpponent(loadedMessages, messageContentText)

        val dialogEntity = GetDialogByIdUseCase(createdDialog?.getDialogId()!!).execute()

        val pagination = PaginationEntityImpl().apply {
            setPerPage(1)
        }

        val result = GetAllMessagesUseCase(dialogEntity!!, pagination).execute().toList()
        val message = result[0].getOrThrow().first!! as IncomingChatMessageEntity
        assertTrue(message.isForwarded())
        assertTrue(message.getForwardedRepliedMessages()!!.isNotEmpty())
        assertEquals(messageCount, message.getForwardedRepliedMessages()!!.size)
        assertEquals(OPPONENT_ID, message.getForwardedRepliedMessages()!![0].getSender()!!.getUserId())
        assertNotNull(message.getForwardedRepliedMessages()!![0].getRelatedMessageId())
        assertEquals(messageContentText, message.getContent())
        assertEquals(messageContentText, message.getForwardedRepliedMessages()!![0].getContent())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun sendFiveTextForwardedMessagesFromLoggedUser_execute_messageIsForwardedAndOutgoing(): Unit = runBlocking {
        val messageCount = 5

        val forwardMessageText = "logged_user_forwarded_test_message_${System.currentTimeMillis()}"

        sendXMessagesByLoggedUser(createdDialog?.getDialogId()!!, forwardMessageText, messageCount)
        val loadedMessages = loadXChatMessages(messagesCount = messageCount)

        val relatedMessageText = "related_message_${System.currentTimeMillis()}"
        val relatedMessage = buildRelatedMessage(createdDialog?.getDialogId()!!, relatedMessageText)

        val createdMessage = CreateForwardMessageUseCase(loadedMessages, relatedMessage).execute()

        SendForwardReplyMessageUseCase(createdMessage!!, createdDialog?.getDialogId()!!).execute()

        val dialogEntity = GetDialogByIdUseCase(createdDialog?.getDialogId()!!).execute()

        val pagination = PaginationEntityImpl().apply {
            setPerPage(1)
        }

        val result = GetAllMessagesUseCase(dialogEntity!!, pagination).execute().toList()
        val message = result[0].getOrThrow().first!! as OutgoingChatMessageEntity
        assertTrue(message.isForwarded())
        assertTrue(message.getForwardedRepliedMessages()!!.isNotEmpty())
        assertEquals(messageCount, message.getForwardedRepliedMessages()!!.size)
        assertEquals(USER_ID, message.getForwardedRepliedMessages()!![0].getSender()!!.getUserId())
        assertNotNull(message.getForwardedRepliedMessages()!![0].getRelatedMessageId())
        assertEquals(relatedMessageText, message.getContent())
        assertEquals(forwardMessageText, message.getForwardedRepliedMessages()!![0].getContent())
    }

    private fun sendXMessagesByOpponent(dialogId: String, messageBodyText: String, messagesCount: Int = 5) {
        val messages = createMessagesFromOpponent(dialogId, messageBodyText, messagesCount)

        for (message in messages) {
            sendChatMessageToDialog(message, opponentChatService!!)
        }
    }

    private fun createMessagesFromOpponent(
        dialogId: String, messageBodyText: String, qbChatMessageCount: Int = 5
    ): List<QBChatMessage> {
        val builtMessages = mutableListOf<QBChatMessage>()

        for (index in 1..qbChatMessageCount) {
            builtMessages.add(createMessageFromOpponent(dialogId, messageBodyText))
        }

        return builtMessages
    }

    private fun sendChatMessageToDialog(qbChatMessage: QBChatMessage, chatService: QBChatService) {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = qbChatMessage.dialogId
        qbDialog.setOccupantsIds(listOf(USER_ID))
        qbDialog.type = QBDialogType.PRIVATE

        qbDialog.initForChat(chatService)

        qbDialog.sendMessage(qbChatMessage)
    }

    private fun createMessageFromOpponent(dialogId: String, messageBodyText: String): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialogId
        qbChatMessage.body = messageBodyText
        qbChatMessage.setSaveToHistory(true)

        return qbChatMessage
    }

    private suspend fun sendXMessagesByLoggedUser(dialogId: String, messageBodyText: String, messagesCount: Int = 5) {
        val messages = createMessagesFromLoggedUser(dialogId, messageBodyText, messagesCount)

        for (message in messages) {
            SendChatMessageUseCase(message).execute()
        }
    }

    private fun createMessagesFromLoggedUser(
        dialogId: String, messageBodyText: String, messageCount: Int = 5
    ): List<OutgoingChatMessageEntity> {
        val builtMessages = mutableListOf<OutgoingChatMessageEntity>()

        for (index in 1..messageCount) {
            builtMessages.add(createMessageFromLoggedUser(dialogId, messageBodyText))
        }

        return builtMessages
    }

    private fun createMessageFromLoggedUser(dialogId: String, messageBodyText: String): OutgoingChatMessageEntity {
        val message = OutgoingChatMessageEntityImpl(
            OutgoingChatMessageEntity.OutgoingStates.SENT, ChatMessageEntity.ContentTypes.TEXT
        )
        message.setParticipantId(OPPONENT_ID)
        message.setDialogId(dialogId)
        message.setContent(messageBodyText)

        return message
    }

    private fun buildRelatedMessage(dialogId: String, messageBodyText: String): OutgoingChatMessageEntity {
        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT)
        message.setDialogId(dialogId)
        message.setParticipantId(OPPONENT_ID)
        message.setContent(messageBodyText)
        message.setTime(System.currentTimeMillis())

        return message
    }

    private suspend fun loadXChatMessages(messagesCount: Int = 5): List<ForwardedRepliedMessageEntity> {
        val pagination = PaginationEntityImpl()
        pagination.setPerPage(10)
        pagination.setCurrentPage(1)

        val result = GetAllMessagesUseCase(createdDialog!!, pagination).execute().toList()

        val loadedMessages = mutableListOf<ForwardedRepliedMessageEntity>()

        result.forEach {
            val message = it.getOrThrow().first!!
            val isChatMessage = message.getMessageType() == MessageEntity.MessageTypes.CHAT

            if (isChatMessage && loadedMessages.count() < messagesCount) {
                val chatMessage = it.getOrThrow().first!! as ForwardedRepliedMessageEntity
                loadedMessages.add(chatMessage)
            }
        }

        return loadedMessages
    }

    private fun sendForwardedMessageByOpponent(messages: List<ForwardedRepliedMessageEntity>, messageBodyText: String) {
        val qbChatMessage = createMessageFromOpponent(createdDialog?.getDialogId()!!, messageBodyText)
        val properties = ForwardReplyMessageParser.parseForwardPropertiesFrom(messages)

        for ((key, value) in properties) {
            qbChatMessage.setProperty(key, value)
        }

        sendChatMessageToDialog(qbChatMessage, opponentChatService!!)
    }
}