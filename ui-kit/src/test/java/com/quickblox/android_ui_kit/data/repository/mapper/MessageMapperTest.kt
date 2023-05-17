/*
 * Created by Injoit on 5.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MessageMapperTest {
    @Test
    fun dtoWithVideoContentType_isExistFile_receivedTrue() {
        val dto = buildRemoteMessageDTO()
        dto.fileName = "test_file_name"
        dto.fileUrl = "https://test.com/a.mp4"
        dto.mimeType = "video/mp4"

        val fileExist = MessageMapper.isExistFileIn(dto)
        assertTrue(fileExist)
    }

    @Test
    fun dtoWithAudioContentType_isExistFile_receivedTrue() {
        val dto = buildRemoteMessageDTO()
        dto.fileName = "test_file_name"
        dto.fileUrl = "https://test.com/a.mp3"
        dto.mimeType = "audio/mpeg"

        val fileExist = MessageMapper.isExistFileIn(dto)
        assertTrue(fileExist)
    }

    @Test
    fun dtoWithTextContentType_isExistFile_receivedFalse() {
        val dto = buildRemoteMessageDTO()
        val fileExist = MessageMapper.isExistFileIn(dto)
        assertFalse(fileExist)
    }

    @Test
    fun dtoWithVideoContentType_getFileEntity_receivedEntity() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.mp4"
        dto.fileUrl = fileUrl

        val mimeType = "video/mp4"
        dto.mimeType = mimeType

        val fileEntity = MessageMapper.getMediaContentFrom(dto)
        assertEquals(MediaContentEntity.Types.VIDEO, fileEntity.getType())
        assertEquals(fileName, fileEntity.getName())
        assertEquals(fileUrl, fileEntity.getUrl())
        assertEquals(mimeType, fileEntity.getMimeType())
    }

    @Test
    fun dtoWithAudioContentType_getFileEntity_receivedEntity() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.mp3"
        dto.fileUrl = fileUrl

        val mimeType = "audio/mpeg"
        dto.mimeType = mimeType

        val fileEntity = MessageMapper.getMediaContentFrom(dto)
        assertEquals(MediaContentEntity.Types.AUDIO, fileEntity.getType())
        assertEquals(fileName, fileEntity.getName())
        assertEquals(fileUrl, fileEntity.getUrl())
        assertEquals(mimeType, fileEntity.getMimeType())
    }

    @Test
    fun dtoWithImageContentType_getFileEntity_receivedEntity() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.jpeg"
        dto.fileUrl = fileUrl

        val mimeType = "image/jpeg"
        dto.mimeType = mimeType

        val fileEntity = MessageMapper.getMediaContentFrom(dto)
        assertEquals(MediaContentEntity.Types.IMAGE, fileEntity.getType())
        assertEquals(fileName, fileEntity.getName())
        assertEquals(fileUrl, fileEntity.getUrl())
        assertEquals(mimeType, fileEntity.getMimeType())
    }

    @Test
    fun dtoWithFileContentType_getFileEntity_receivedEntity() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.pdf"
        dto.fileUrl = fileUrl

        val mimeType = "application/pdf"
        dto.mimeType = mimeType

        val mediaContent = MessageMapper.getMediaContentFrom(dto)
        assertEquals(MediaContentEntity.Types.FILE, mediaContent.getType())
        assertEquals(fileName, mediaContent.getName())
        assertEquals(fileUrl, mediaContent.getUrl())
        assertEquals(mimeType, mediaContent.getMimeType())
    }

    @Test
    fun dtoWithAudioContentType_getContentType_receivedMediaContentType() {
        val dto = buildRemoteMessageDTO()
        dto.fileName = "test_file_name"
        dto.fileUrl = "https://test.com/a.mp3"
        dto.mimeType = "audio/mpeg"

        val contentType = MessageMapper.getContentTypeFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.MEDIA, contentType)
    }

    @Test
    fun dtoWithVideoContentType_getContentType_receivedMediaContentType() {
        val dto = buildRemoteMessageDTO()
        dto.fileName = "test_file_name"
        dto.fileUrl = "https://test.com/a.mp4"
        dto.mimeType = "video/mp4"

        val contentType = MessageMapper.getContentTypeFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.MEDIA, contentType)
    }

    @Test
    fun dtoWithTextContentType_getContentType_receivedTextContentType() {
        val dto = buildRemoteMessageDTO()
        val contentType = MessageMapper.getContentTypeFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.TEXT, contentType)
    }

    @Test
    fun dtoWithVideoContentType_dtoToChatEntity_receivedEntityWithMediaContentType() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.mp4"
        dto.fileUrl = fileUrl

        val mimeType = "video/mp4"
        dto.mimeType = mimeType

        val chatEntity = MessageMapper.incomingChatEntityFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.MEDIA, chatEntity.getContentType())
        assertEquals(fileName, chatEntity.getMediaContent()!!.getName())
        assertEquals(fileUrl, chatEntity.getMediaContent()!!.getUrl())
        assertEquals(mimeType, chatEntity.getMediaContent()!!.getMimeType())
    }

    @Test
    fun dtoWithTextContentType_dtoToChatEntity_receivedEntityWithTextContentType() {
        val dto = buildRemoteMessageDTO()
        val chatEntity = MessageMapper.incomingChatEntityFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.TEXT, chatEntity.getContentType())
    }

    @Test
    fun dtoWithAudioContentType_dtoToChatEntity_receivedEntityWithMediaContentType() {
        val dto = buildRemoteMessageDTO()

        val fileName = "test_file_name"
        dto.fileName = fileName

        val fileUrl = "https://test.com/a.mp"
        dto.fileUrl = fileUrl

        val mimeType = "audio/mpeg"
        dto.mimeType = mimeType

        val chatEntity = MessageMapper.incomingChatEntityFrom(dto)
        assertEquals(ChatMessageEntity.ContentTypes.MEDIA, chatEntity.getContentType())
        assertEquals(fileName, chatEntity.getMediaContent()!!.getName())
        assertEquals(fileUrl, chatEntity.getMediaContent()!!.getUrl())
        assertEquals(mimeType, chatEntity.getMediaContent()!!.getMimeType())
    }

    private fun buildRemoteMessageDTO(): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.dialogId = "dialogId"
        dto.id = "messageId"
        dto.time = System.currentTimeMillis()
        dto.text = "text"
        dto.senderId = Random.nextInt(100, 1000)
        dto.participantId = Random.nextInt(100, 1000)

        return dto
    }
}