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

private const val MAX_MEGABYTES_FILE_LENGTH = 10

class CreateMessageUseCase(
    private val contentType: ChatMessageEntity.ContentTypes,
    private val dialogId: String,
    private val content: String? = null,
    private val fileEntity: FileEntity? = null,
) : FlowUseCase<OutgoingChatMessageEntity?>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): Flow<OutgoingChatMessageEntity?> {
        if (isTextMessage(contentType) && content?.isBlank() == true) {
            throw DomainException("The content parameter should not be empty for Text message")
        }

        if (isMediaMessage(contentType) && isNotCorrectFileSize(fileEntity?.getFile(), MAX_MEGABYTES_FILE_LENGTH)) {
            throw DomainException("The file size more then max supported $MAX_MEGABYTES_FILE_LENGTH megabytes")
        }

        if (isMediaMessage(contentType) && isNotCorrectFile(fileEntity)) {
            throw DomainException("The file has wrong URL or MimeType")
        }

        return channelFlow {
            var createdMessage: OutgoingChatMessageEntity? = null

            withContext(Dispatchers.IO) {
                runCatching {
                    val localMessage: OutgoingChatMessageEntity
                    val loggedUserId = usersRepository.getLoggedUserId()

                    if (contentType == ChatMessageEntity.ContentTypes.MEDIA) {
                        val mediaContent = createMediaContentWithoutUploadFileFrom(fileEntity!!)
                        localMessage = createMediaMessage(dialogId, mediaContent, loggedUserId)
                    } else {
                        localMessage = createTextMessage(dialogId, content!!, loggedUserId)
                    }

                    createdMessage = messagesRepository.createMessage(localMessage)

                    send(createdMessage)

                    if (contentType == ChatMessageEntity.ContentTypes.MEDIA) {
                        val mediaContent = createMediaContentWithUploadFileFrom(fileEntity!!)
                        createdMessage?.setMediaContent(mediaContent)

                        send(createdMessage)
                    }
                }.onFailure { error ->
                    val tmp = error.message
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
    fun isNotCorrectFile(file: FileEntity?): Boolean {
        val isIncorrectFileLength = isNotCorrectFileSize(file?.getFile(), MAX_MEGABYTES_FILE_LENGTH)

        val isIncorrectFileUrl = TextUtils.isEmpty(file?.getUrl())
        val isIncorrectFileMimeType = TextUtils.isEmpty(file?.getMimeType())

        return isIncorrectFileLength || isIncorrectFileUrl || isIncorrectFileMimeType
    }

    private fun isNotCorrectFileSize(file: File?, maxMegaBytesFileLength: Int): Boolean {
        if (file == null) {
            return true
        }

        val bytesFileLength = file.length()
        val kiloBytesFileLength = bytesFileLength / 1024
        val megaBytesFileLength = kiloBytesFileLength / 1024

        return megaBytesFileLength > maxMegaBytesFileLength
    }

    @VisibleForTesting
    fun createTextMessage(dialogId: String, content: String, loggedUserId: Int): OutgoingChatMessageEntity {
        val message = createMessage(dialogId, content, ChatMessageEntity.ContentTypes.TEXT, loggedUserId)
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
    fun createMediaMessage(
        dialogId: String,
        mediaContentEntity: MediaContentEntity,
        loggedUserId: Int,
    ): OutgoingChatMessageEntity {
        val message = createMessage(dialogId, null, ChatMessageEntity.ContentTypes.MEDIA, loggedUserId)
        message.setMediaContent(mediaContentEntity)

        return message
    }

    @VisibleForTesting
    fun createMessage(
        dialogId: String,
        content: String?,
        type: ChatMessageEntity.ContentTypes,
        loggedUserId: Int,
    ): OutgoingChatMessageEntity {
        val message = OutgoingChatMessageEntityImpl(OutgoingChatMessageEntity.OutgoingStates.SENDING, type)
        message.setTime(System.currentTimeMillis() / 1000)
        message.setDialogId(dialogId)
        message.setContent(content)
        message.setSenderId(loggedUserId)

        return message
    }
}