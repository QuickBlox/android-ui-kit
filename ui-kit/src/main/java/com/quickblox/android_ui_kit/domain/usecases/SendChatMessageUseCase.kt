/*
 * Created by Injoit on 30.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendChatMessageUseCase(private val message: OutgoingChatMessageEntity) :
    BaseUseCase<OutgoingChatMessageEntity>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()

    override suspend fun execute(): OutgoingChatMessageEntity {
        if (message.getDialogId().isNullOrEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        val isNotTextMessage = message.getContentType() != ChatMessageEntity.ContentTypes.TEXT
        if (isNotTextMessage && isMediaContentNotAvailableIn(message)) {
            throw DomainException("The File shouldn't be empty if message not a text")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val dialog = getDialogBy(message.getDialogId()!!)

                // TODO: temporary solution, need to be deleted in 2nd iteration when we will have message cache
                if (message.getMediaContent() != null) {
                    message.setContent(makeMessageBodyFromMediaContent(message.getMediaContent()!!))
                }
                messagesRepository.sendChatMessageToRemote(message, dialog)

                message.setOutgoingState(OutgoingChatMessageEntity.OutgoingStates.SENT)
            }.onFailure { error ->
                message.setOutgoingState(OutgoingChatMessageEntity.OutgoingStates.ERROR)
            }

            message.setTime(System.currentTimeMillis() / 1000)
        }

        return message
    }

    private fun isMediaContentNotAvailableIn(message: OutgoingChatMessageEntity): Boolean {
        return message.getMediaContent()?.getUrl()?.isEmpty() == true
    }

    private fun getDialogBy(dialogId: String): DialogEntity {
        return dialogsRepository.getDialogFromLocal(dialogId)
    }

    // TODO: temporary solution, need to be deleted in 2nd iteration when we will have message cache
    private fun makeMessageBodyFromMediaContent(mediaContentEntity: MediaContentEntity): String {
        val fileName = mediaContentEntity.getName()
        val fileUrl = mediaContentEntity.getUrl()
        val fileMimeType = mediaContentEntity.getMimeType()

        val messageBody = "${MediaContentEntity::class.java.simpleName}|$fileName|$fileUrl|$fileMimeType"
        return messageBody
    }
}