/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIRephraseWithApiKeyUseCase(private val dialogId: String?, private val toneEntity: AIRephraseEntity) :
    BaseUseCase<AIRephraseEntity?>() {
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()
    private val messageRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): AIRephraseEntity? {
        var resultEntity: AIRephraseEntity? = null

        withContext(Dispatchers.IO) {
            var messagesFromUIKit = listOf<MessageEntity>()
            try {
                dialogId?.let {
                    messagesFromUIKit = loadMessages(it)
                }

                resultEntity = aiRepository.rephraseWithApiKey(toneEntity, messagesFromUIKit)
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            } catch (exception: MessagesRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return resultEntity
    }

    private suspend fun loadMessages(
        dialogId: String,
    ): List<MessageEntity> {
        val messages = mutableListOf<MessageEntity>()

        var pagination = PaginationEntityImpl().apply {
            setCurrentPage(0)
            setHasNextPage(true)
        }

        pagination.setHasNextPage(true)

        val MAX_PAGES_COUNT = 5
        if (pagination.hasNextPage() && pagination.getCurrentPage() < MAX_PAGES_COUNT) {
            messageRepository.getMessagesFromRemote(dialogId, pagination).collect { pairResult ->
                val receivedMessage = pairResult.getOrThrow().first
                val receivedPagination = pairResult.getOrThrow().second

                pagination = receivedPagination as PaginationEntityImpl

                receivedMessage?.let {
                    val isTextMessage =
                        receivedMessage is ChatMessageEntity && receivedMessage.getContentType() == ChatMessageEntity.ContentTypes.TEXT
                    if (isTextMessage) {
                        messages.add(receivedMessage)
                    }
                }
            }
        }

        return messages
    }
}