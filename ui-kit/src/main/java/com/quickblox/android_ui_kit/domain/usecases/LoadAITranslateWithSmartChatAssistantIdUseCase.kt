/*
 * Created by Injoit on 12.9.2024.
 * Copyright © 2024 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.Language
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LoadAITranslateWithSmartChatAssistantIdUseCase(
    private val dialogId: String?,
    private val message: ForwardedRepliedMessageEntity,
) :
    BaseUseCase<AITranslateIncomingChatMessageEntity?>() {
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()
    private val messageRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): AITranslateIncomingChatMessageEntity? {
        var entity: AITranslateIncomingChatMessageEntity? = null

        withContext(Dispatchers.IO) {
            var messagesFromUIKit = listOf<MessageEntity>()
            dialogId?.let {
                val pagination = searchStartPagination(dialogId, message)
                messagesFromUIKit = loadMessages(dialogId, message, pagination)
            }

            val languageCode: String = getDefaultLanguageCode()

            try {
                entity = aiRepository.translateIncomingMessageWithSmartChatAssistantId(
                    message,
                    messagesFromUIKit,
                    languageCode
                )
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }
        entity?.setRelatedMessageId(message.getRelatedMessageId())
        return entity
    }

    private fun getDefaultLanguageCode(): String {
        val defaultLanguageCode: String = Locale.getDefault().language

        val isSupportDefaultLanguage = Language.isLanguageSupported(defaultLanguageCode)
        return if (isSupportDefaultLanguage) {
            defaultLanguageCode
        } else {
            Language.English.code
        }
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