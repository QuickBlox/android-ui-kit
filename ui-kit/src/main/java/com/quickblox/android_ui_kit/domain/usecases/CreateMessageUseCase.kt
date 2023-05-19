/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.io.File

class CreateMessageUseCase(
    private val contentType: ChatMessageEntity.ContentTypes,
    private val dialogId: String,
    private val content: String? = null,
    private val fileEntity: FileEntity? = null
) : FlowUseCase<OutgoingChatMessageEntity?>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()
    override suspend fun execute(): Flow<OutgoingChatMessageEntity?> {
        if (isTextMessage(contentType) && content?.isBlank() == true) {
            throw DomainException("The content parameter should not be empty for Text message")
        }

        if (isMediaMessage(contentType) && isIncorrectFile(fileEntity)) {
            throw DomainException("The file parameter should not be empty for Media message")
        }

        return channelFlow {
            var createdMessage: OutgoingChatMessageEntity? = null

            withContext(Dispatchers.IO) {
                runCatching {
                    val localMessage: OutgoingChatMessageEntity
                    if (contentType == ChatMessageEntity.ContentTypes.MEDIA) {
                        val mediaContent = createMediaContentWithoutUploadFileFrom(fileEntity!!)
                        localMessage = createMediaMessage(dialogId, mediaContent)
                    } else {
                        localMessage = createTextMessage(dialogId, content!!)
                    }

                    createdMessage = messagesRepository.createMessage(localMessage)

                    send(createdMessage)

                    if (contentType == ChatMessageEntity.ContentTypes.MEDIA) {
                        val mediaContent = createMediaContentWithUploadFileFrom(fileEntity!!)
                        createdMessage?.setMediaContent(mediaContent)

                        send(createdMessage)
                    }
                }.onFailure { error ->
                    createdMessage?.let { message ->
                        message.setOutgoingState(OutgoingChatMessageEntity.OutgoingStates.ERROR)
                        send(message)
                    }
                }
            }
        }
    }

    @VisibleForTesting
    fun isTextMessage(type: ChatMessageEntity.ContentTypes): Boolean {
        return type == ChatMessageEntity.ContentTypes.TEXT
    }

    @VisibleForTesting
    fun isMediaMessage(type: ChatMessageEntity.ContentTypes): Boolean {
        return type == ChatMessageEntity.ContentTypes.MEDIA
    }

    @VisibleForTesting
    fun isIncorrectFile(file: FileEntity?): Boolean {
        val fileNotHasLength = file?.getFile()?.length() == null
        val isIncorrectFileLength = fileNotHasLength || file?.getFile()?.length()!! <= 0

        val isIncorrectFileUrl = TextUtils.isEmpty(file?.getUrl())
        val isIncorrectFileMimeType = TextUtils.isEmpty(file?.getMimeType())

        return isIncorrectFileLength || isIncorrectFileUrl || isIncorrectFileMimeType
    }

    @VisibleForTesting
    fun createTextMessage(dialogId: String, content: String): OutgoingChatMessageEntity {
        val message = createMessage(dialogId, content, ChatMessageEntity.ContentTypes.TEXT)
        return message
    }

    @VisibleForTesting
    fun createMediaContentWithoutUploadFileFrom(fileEntity: FileEntity): MediaContentEntity {
        val fileName = fileEntity.getFile()!!.name
        val fileUrl = fileEntity.getUrl()!!
        val fileMimeType = fileEntity.getMimeType()!!

        return createMediaContent(fileName, fileUrl, fileMimeType)
    }

    @VisibleForTesting
    fun createMediaContentWithUploadFileFrom(fileEntity: FileEntity): MediaContentEntity {
        val filePair = uploadFile(fileEntity.getFile()!!)

        val fileUrl = filePair.first
        val fileMimeType = filePair.second

        return createMediaContent(fileEntity.getFile()!!.name, fileUrl, fileMimeType)
    }

    @VisibleForTesting
    fun createMediaContent(fileName: String, fileUrl: String, mimeType: String): MediaContentEntity {
        return MediaContentEntityImpl(fileName, fileUrl, mimeType)
    }

    private fun uploadFile(file: File): Pair<String, String> {
        val fileEntity = FileEntityImpl()
        fileEntity.setFile(file)

        val savedFileEntity = filesRepository.saveFileToRemote(fileEntity)
        val fileUrl = savedFileEntity.getUrl()
        val mimeType = savedFileEntity.getMimeType()

        val isNotCorrectFileUrl = fileUrl == null || fileUrl.isEmpty()
        val isNotCorrectMimeType = mimeType == null || mimeType.isEmpty()
        if (isNotCorrectFileUrl || isNotCorrectMimeType) {
            throw RuntimeException("The file has wrong value for fileUrl or mimeType")
        }

        return Pair(fileUrl!!, mimeType!!)
    }

    @VisibleForTesting
    fun createMediaMessage(dialogId: String, mediaContentEntity: MediaContentEntity): OutgoingChatMessageEntity {
        val message = createMessage(dialogId, null, ChatMessageEntity.ContentTypes.MEDIA)
        message.setMediaContent(mediaContentEntity)

        return message
    }

    @VisibleForTesting
    fun createMessage(
        dialogId: String,
        content: String?,
        type: ChatMessageEntity.ContentTypes
    ): OutgoingChatMessageEntity {
        val message = OutgoingChatMessageEntityImpl(OutgoingChatMessageEntity.OutgoingStates.SENDING, type)
        message.setTime(System.currentTimeMillis() / 1000)
        message.setDialogId(dialogId)
        message.setContent(content)

        return message
    }
}