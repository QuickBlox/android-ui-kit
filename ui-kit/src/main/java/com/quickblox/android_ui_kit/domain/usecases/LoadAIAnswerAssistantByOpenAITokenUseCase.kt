/*
 * Created by Injoit on 8.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ai_answer_assistant.QBAIAnswerAssistant
import com.quickblox.android_ai_answer_assistant.exception.QBAIAnswerAssistantException
import com.quickblox.android_ai_answer_assistant.message.Message
import com.quickblox.android_ai_answer_assistant.message.OpponentMessage
import com.quickblox.android_ai_answer_assistant.message.OwnerMessage
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIAnswerAssistantByOpenAITokenUseCase(
    private val dialogId: String,
    private val message: IncomingChatMessageEntity
) :
    BaseUseCase<List<String>>() {
    private val messageRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): List<String> {
        val receivedAnswers = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            val pagination = searchStartPagination(dialogId, message)
            val messagesFromUIKit = loadMessages(dialogId, message, pagination)
            val messagesFromAI = convertUIKitMessagesToAIMessages(messagesFromUIKit)

            val openAIToken = QuickBloxUiKit.getOpenAIToken()

            try {
                val answers = QBAIAnswerAssistant.executeByOpenAITokenSync(openAIToken, messagesFromAI)
                receivedAnswers.addAll(answers)
            } catch (exception: QBAIAnswerAssistantException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return receivedAnswers
    }

    private suspend fun searchStartPagination(
        dialogId: String,
        incomingMessage: IncomingChatMessageEntity
    ): PaginationEntity {
        var paginationResult: PaginationEntity = PaginationEntityImpl()
        paginationResult.setHasNextPage(true)

        var isNotFoundPagination = true

        while (paginationResult.hasNextPage()) {
            messageRepository.getMessagesFromRemote(dialogId, paginationResult).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                paginationResult = receivedPagination

                val isEqualsMessages = receivedMessage?.getMessageId() == incomingMessage.getMessageId()
                if (isNotFoundPagination && isEqualsMessages) {
                    isNotFoundPagination = false
                    return@collect
                }
            }

            if (isNotFoundPagination) {
                paginationResult.nextPage()
            }
        }

        if (isNotFoundPagination) {
            throw DomainException("Didn't find message with id: ${incomingMessage.getMessageId()}")
        }

        return paginationResult
    }

    private suspend fun loadMessages(
        dialogId: String,
        startMessage: IncomingChatMessageEntity,
        paginationEntity: PaginationEntity
    ): List<MessageEntity> {
        val messages = mutableListOf<MessageEntity>()

        var pagination = paginationEntity
        pagination.setHasNextPage(true)

        var foundStartMessage = false

        val MAX_PAGES_COUNT = 5
        if (pagination.hasNextPage() && pagination.getCurrentPage() < MAX_PAGES_COUNT) {
            messageRepository.getMessagesFromRemote(dialogId, pagination).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                pagination = receivedPagination

                receivedMessage?.let {
                    if (receivedMessage.getMessageId() == startMessage.getMessageId()) {
                        foundStartMessage = true
                    }

                    val isTextMessage =
                        receivedMessage is ChatMessageEntity && receivedMessage.getContentType() == ChatMessageEntity.ContentTypes.TEXT
                    if (foundStartMessage && isTextMessage) {
                        messages.add(receivedMessage)
                    }
                }
            }
        }

        return messages
    }

    private fun convertUIKitMessagesToAIMessages(uiKitMessages: List<MessageEntity>): List<Message> {
        val aiMessages = mutableListOf<Message>()

        uiKitMessages.forEach { messageEntity ->
            if (messageEntity is IncomingChatMessageEntity) {
                messageEntity.getContent()?.let {
                    val aiMessage = OpponentMessage(it)
                    aiMessages.add(aiMessage)
                }
            }
            if (messageEntity is OutgoingChatMessageEntity) {
                messageEntity.getContent()?.let {
                    val aiMessage = OwnerMessage(it)
                    aiMessages.add(aiMessage)
                }
            }
        }

        return aiMessages
    }
}