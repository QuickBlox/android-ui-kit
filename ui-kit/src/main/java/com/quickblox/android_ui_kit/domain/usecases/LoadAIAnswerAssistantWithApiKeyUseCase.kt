/*
 * Created by Injoit on 8.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ai_answer_assistant.exception.QBAIAnswerAssistantException
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIAnswerAssistantWithApiKeyUseCase(
    private val dialogId: String,
    private val message: ForwardedRepliedMessageEntity,
) : BaseUseCase<String>() {
    private val messageRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()

    override suspend fun execute(): String {
        val receivedAnswer: String

        withContext(Dispatchers.IO) {
            try {
                val pagination = searchStartPagination(dialogId, message)
                val messagesFromUIKit = loadMessages(dialogId, message, pagination)

                receivedAnswer = aiRepository.createAnswerWithApiKey(messagesFromUIKit)
            } catch (exception: QBAIAnswerAssistantException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            } catch (exception: MessagesRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return receivedAnswer
    }

    private suspend fun searchStartPagination(
        dialogId: String,
        incomingMessage: ForwardedRepliedMessageEntity,
    ): PaginationEntity {
        var paginationResult: PaginationEntity = PaginationEntityImpl()
        paginationResult.setHasNextPage(true)

        var isNotFoundPagination = true

        while (paginationResult.hasNextPage()) {
            messageRepository.getMessagesFromRemote(dialogId, paginationResult).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                paginationResult = receivedPagination

                val messageId = incomingMessage.getRelatedMessageId() ?: incomingMessage.getMessageId()
                val isEqualsMessages = receivedMessage?.getMessageId() == messageId
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
        startMessage: ForwardedRepliedMessageEntity,
        paginationEntity: PaginationEntity,
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

                val messageId = startMessage.getRelatedMessageId() ?: startMessage.getMessageId()
                receivedMessage?.let {
                    if (receivedMessage.getMessageId() == messageId) {
                        foundStartMessage = true
                        if (receivedMessage is ChatMessageEntity) {
                            if (startMessage is AITranslateIncomingChatMessageEntity && startMessage.isTranslated()) {
                                receivedMessage.setContent(startMessage.getTranslation())
                            } else {
                                receivedMessage.setContent(startMessage.getContent())
                            }
                        }
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
}