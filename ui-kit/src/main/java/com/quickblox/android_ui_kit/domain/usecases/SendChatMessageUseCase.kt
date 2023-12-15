/*
 * Created by Injoit on 30.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
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

    @VisibleForTesting
    fun isMediaContentNotAvailableIn(message: OutgoingChatMessageEntity): Boolean {
        return message.getMediaContent()?.getUrl()?.isBlank() ?: true
    }

    private fun getDialogBy(dialogId: String): DialogEntity {
        return dialogsRepository.getDialogFromLocal(dialogId)
    }

    // TODO: temporary solution, need to be deleted in 2nd iteration when we will have message cache
    @VisibleForTesting
    fun makeMessageBodyFromMediaContent(mediaContentEntity: MediaContentEntity): String {
        val fileName = mediaContentEntity.getName()
        val uid = getUidFom(mediaContentEntity)
        val fileMimeType = mediaContentEntity.getMimeType()

        val messageBody = "${MediaContentEntity::class.java.simpleName}|$fileName|$uid|$fileMimeType"
        return messageBody
    }

    private fun getUidFom(mediaContentEntity: MediaContentEntity): String {
        if (mediaContentEntity.getUrl().contains("blobs")) {
            return try {
                val splitUrl = mediaContentEntity.getUrl().split("/")
                splitUrl[4].replace(".json", "").split("?")[0]
            } catch (e: IndexOutOfBoundsException) {
                ""
            }
        } else {
            return mediaContentEntity.getUrl()
        }
    }
}