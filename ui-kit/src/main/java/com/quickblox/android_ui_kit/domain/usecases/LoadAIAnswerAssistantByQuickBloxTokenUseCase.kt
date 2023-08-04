/*
 * Created by Injoit on 8.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ai_answer_assistant.QBAIAnswerAssistant
import com.quickblox.android_ai_answer_assistant.exception.QBAIAnswerAssistantException
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
class LoadAIAnswerAssistantByQuickBloxTokenUseCase(
    private val dialogId: String,
    private val message: IncomingChatMessageEntity
) :
    BaseUseCase<List<String>>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): List<String> {
        val receivedAnswers = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            val pagination = searchStartPagination(dialogId, message)
            val messagesFromUIKit = loadMessages(dialogId, pagination)
            val messagesFromAI = convertUIKitMessagesToAIMessages(messagesFromUIKit)

            val proxyServerURL = QuickBloxUiKit.getProxyServerURL()
            val token = usersRepository.getUserSessionToken()

            try {
                val answers = QBAIAnswerAssistant.executeByQBTokenSync(token, proxyServerURL, messagesFromAI, true)
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
            messagesRepository.getMessagesFromRemote(dialogId, paginationResult).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                paginationResult = receivedPagination

                val isEqualsMessages = receivedMessage?.geMessageId() == incomingMessage.geMessageId()
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
            throw DomainException("Didn't find message with id: ${incomingMessage.geMessageId()}")
        }

        return paginationResult
    }

    private suspend fun loadMessages(
        dialogId: String,
        paginationEntity: PaginationEntity
    ): List<MessageEntity> {
        val messages = mutableListOf<MessageEntity>()

        var pagination = paginationEntity
        pagination.setHasNextPage(true)

        if (pagination.hasNextPage() && pagination.getCurrentPage() < 5) {
            messagesRepository.getMessagesFromRemote(dialogId, pagination).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                pagination = receivedPagination

                receivedMessage?.let {
                    if (receivedMessage is ChatMessageEntity && receivedMessage.getContentType() == ChatMessageEntity.ContentTypes.TEXT) {
                        messages.add(receivedMessage)
                    }
                }
            }
        }

        return messages
    }

    private fun convertUIKitMessagesToAIMessages(uiKitMessages: List<MessageEntity>): List<com.quickblox.android_ai_answer_assistant.message.Message> {
        val aiMessages = mutableListOf<com.quickblox.android_ai_answer_assistant.message.Message>()

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