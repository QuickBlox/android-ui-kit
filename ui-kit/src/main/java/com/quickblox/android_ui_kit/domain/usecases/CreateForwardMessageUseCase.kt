/*
 * Created by Injoit on 13.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateForwardMessageUseCase(
    private val forwardMessages: List<ForwardedRepliedMessageEntity>,
    private var relateMessage: OutgoingChatMessageEntity? = null
) : BaseUseCase<OutgoingChatMessageEntity?>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): OutgoingChatMessageEntity? {
        if (forwardMessages.isEmpty()) {
            throw DomainException("The forwardMessages parameter shouldn't be empty")
        }

        val isDialogIdNotExistInRelateMessage = relateMessage?.getDialogId().isNullOrEmpty()
        if (relateMessage != null && isDialogIdNotExistInRelateMessage) {
            throw DomainException("The dialogId shouldn't be empty in relateMessage")
        }

        val isMediaMessage = relateMessage?.getContentType() == ChatMessageEntity.ContentTypes.MEDIA
        if (isMediaMessage && isMediaContentNotAvailableIn(relateMessage)) {
            throw DomainException("The File shouldn't be empty if message is MEDIA")
        }

        val isDialogIdNotExistInForwardMessage = forwardMessages[0].getDialogId().isNullOrEmpty()
        if (isDialogIdNotExistInForwardMessage) {
            throw DomainException("The dialogId shouldn't be empty in forward messages")
        }

        var createdMessage: OutgoingChatMessageEntity? = null

        withContext(Dispatchers.IO) {
            if (relateMessage == null) {
                val dialogId = forwardMessages[0].getDialogId()!!
                relateMessage = createRelateMessage(dialogId)
            }

            markForwarded(relateMessage!!)

            runCatching {
                createdMessage = messagesRepository.createForwardMessage(forwardMessages, relateMessage!!)
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

    @VisibleForTesting
    fun createRelateMessage(dialogId: String): OutgoingChatMessageEntity {
        val message = OutgoingChatMessageEntityImpl(
            OutgoingChatMessageEntity.OutgoingStates.SENDING, ChatMessageEntity.ContentTypes.TEXT
        )

        message.setTime(System.currentTimeMillis() / 1000)
        message.setDialogId(dialogId)
        message.setContent("")

        return message
    }

    @VisibleForTesting
    fun markForwarded(relateMessage: ForwardedRepliedMessageEntity): ForwardedRepliedMessageEntity {
        return relateMessage.apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.FORWARDED)
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