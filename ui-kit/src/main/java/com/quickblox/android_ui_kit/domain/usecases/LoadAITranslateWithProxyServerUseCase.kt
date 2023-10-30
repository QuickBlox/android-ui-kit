/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAITranslateWithProxyServerUseCase(private val dialogId: String?, private val message: IncomingChatMessageEntity) :
    BaseUseCase<AITranslateIncomingChatMessageEntity?>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()
    private val messageRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): AITranslateIncomingChatMessageEntity? {
        var entity: AITranslateIncomingChatMessageEntity? = null

        withContext(Dispatchers.IO) {
            val token = usersRepository.getUserSessionToken()

            var messagesFromUIKit = listOf<MessageEntity>()
            dialogId?.let {
                val pagination = searchStartPagination(dialogId, message)
                messagesFromUIKit = loadMessages(dialogId, message, pagination)
            }

            try {
                entity = aiRepository.translateIncomingMessageWithProxyServer(message, token, messagesFromUIKit)
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return entity
    }

    private suspend fun searchStartPagination(
        dialogId: String,
        incomingMessage: IncomingChatMessageEntity,
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
}