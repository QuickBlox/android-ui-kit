/*
 * Created by Injoit on 13.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateReplyMessageUseCase(
    private val replyMessage: ForwardedRepliedMessageEntity,
    private var relateMessage: OutgoingChatMessageEntity
) : BaseUseCase<OutgoingChatMessageEntity?>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): OutgoingChatMessageEntity? {
        val isDialogIdNotExistInRelateMessage = relateMessage.getDialogId().isNullOrEmpty()
        if (isDialogIdNotExistInRelateMessage) {
            throw DomainException("The dialogId shouldn't be empty in relateMessage")
        }

        val isMediaMessage = relateMessage.getContentType() == ChatMessageEntity.ContentTypes.MEDIA
        if (isMediaMessage && isMediaContentNotAvailableIn(relateMessage)) {
            throw DomainException("The File shouldn't be empty if message is MEDIA")
        }

        val isDialogIdNotExistInReplyMessage = replyMessage.getDialogId().isNullOrEmpty()
        if (isDialogIdNotExistInReplyMessage) {
            throw DomainException("The dialogId shouldn't be empty in reply messages")
        }

        var createdMessage: OutgoingChatMessageEntity? = null

        withContext(Dispatchers.IO) {
            markAsReplied(relateMessage)

            runCatching {
                addSenderIdTo(relateMessage)
                createdMessage = messagesRepository.createReplyMessage(replyMessage, relateMessage)
                createdMessage?.getForwardedRepliedMessages()?.forEach { message ->
                    message.getSenderId()?.let { senderId ->
                        val user = loadUserBy(senderId)
                        message.setSender(user)
                    }
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return createdMessage
    }

    private fun addSenderIdTo(message: ChatMessageEntity) {
        val loggedUserId = usersRepository.getLoggedUserId()
        message.setSenderId(loggedUserId)
    }

    @VisibleForTesting
    fun markAsReplied(relateMessage: ForwardedRepliedMessageEntity): ForwardedRepliedMessageEntity {
        return relateMessage.apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.REPLIED)
        }
    }

    @VisibleForTesting
    fun isMediaContentNotAvailableIn(message: OutgoingChatMessageEntity?): Boolean {
        return message?.getMediaContent()?.getUrl()?.isBlank() ?: true
    }

    private fun loadUserBy(userId: Int): UserEntity? {
        try {
            return usersRepository.getUserFromRemote(userId)
        } catch (exception: UsersRepositoryException) {
            return null
        }
    }
}