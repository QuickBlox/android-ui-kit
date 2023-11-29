/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendForwardReplyMessageUseCase(
    private val forwardReplyMessage: OutgoingChatMessageEntity, private val dialogIdToForwardReply: String
) : BaseUseCase<OutgoingChatMessageEntity>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): OutgoingChatMessageEntity {
        if (dialogIdToForwardReply.isEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val localDialog = dialogsRepository.getDialogFromLocal(dialogIdToForwardReply)

                val mediaContent = forwardReplyMessage.getMediaContent()
                val mediaContentExist = mediaContent != null
                if (mediaContentExist) {
                    val messageBody = makeMessageBodyFromMediaContent(mediaContent!!)
                    forwardReplyMessage.setContent(messageBody)
                }

                sendMessage(forwardReplyMessage, localDialog)

                forwardReplyMessage.setOutgoingState(OutgoingChatMessageEntity.OutgoingStates.SENT)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }

            forwardReplyMessage.setTime(System.currentTimeMillis() / 1000)
        }

        return forwardReplyMessage
    }

    @VisibleForTesting
    fun makeMessageBodyFromMediaContent(mediaContentEntity: MediaContentEntity): String {
        val fileName = mediaContentEntity.getName()
        val uid = getUidFom(mediaContentEntity.getUrl())
        val fileMimeType = mediaContentEntity.getMimeType()

        val messageBody = "${MediaContentEntity::class.java.simpleName}|$fileName|$uid|$fileMimeType"
        return messageBody
    }

    @VisibleForTesting
    fun getUidFom(url: String?): String? {
        if (url != null && url.contains("blobs")) {
            return try {
                val splitUrl = url.split("/")
                val uid = splitUrl[4]
                val uidWithoutJson = uid.replace(".json", "")

                val splitUidWithoutToken = uidWithoutJson.split("?")
                val uidWithoutToken = splitUidWithoutToken[0]

                return uidWithoutToken
            } catch (e: IndexOutOfBoundsException) {
                ""
            }
        } else {
            return url
        }
    }

    @VisibleForTesting
    fun sendMessage(message: OutgoingChatMessageEntity?, dialog: DialogEntity) {
        message?.let {
            messagesRepository.sendChatMessageToRemote(message, dialog)
        }
    }
}