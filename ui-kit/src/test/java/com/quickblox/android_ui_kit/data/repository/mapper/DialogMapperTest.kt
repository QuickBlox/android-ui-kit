/*
 * Created by Injoit on 28.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteDialogDTOMapper
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.*
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import kotlin.random.Random

class DialogMapperTest {
    @Test
    fun buildDialogEntityStub_mappingToRemoteDto_NoException() {
        val entity = buildGroupDialogEntityStub()
        val dto = DialogMapper.dtoRemoteFrom(entity)

        assertEquals(dto.id, entity.getDialogId())
        assertEquals(dto.ownerId, entity.getOwnerId())
        assertEquals(dto.participantIds, entity.getParticipantIds())
        assertEquals(dto.updatedAt, entity.getUpdatedAt())
        assertEquals(dto.lastMessageText, entity.getLastMessage()?.getContent())
        assertEquals(dto.lastMessageDateSent, entity.getLastMessage()?.getTime())
        assertEquals(dto.lastMessageUserId, entity.getLastMessage()?.getSenderId())
        assertEquals(dto.unreadMessageCount, entity.getUnreadMessagesCount())
        assertEquals(dto.name, entity.getName())
        assertEquals(dto.photo, entity.getPhoto())
    }

    @Test(expected = MappingException::class)
    fun buildDialogEntityStubWithTypeNull_mappingToRemoteDto_MappingException() {
        val entity = buildGroupDialogEntityStub()
        entity.setDialogType(null)
        DialogMapper.dtoRemoteFrom(entity)
    }

    @Test(expected = MappingException::class)
    fun buildDialogEntityStubWithTypePublicAndNameNull_mappingToRemoteDto_MappingException() {
        val entity = buildPublicDialogEntityStub()
        entity.setName(null)
        DialogMapper.dtoRemoteFrom(entity)
    }

    @Test(expected = MappingException::class)
    fun buildDialogEntityStubWithTypeGroupAndNameEmpty_mappingToRemoteDto_MappingException() {
        val entity = buildGroupDialogEntityStub()
        entity.setDialogType(GROUP)
        entity.setName("")
        DialogMapper.dtoRemoteFrom(entity)
    }

    @Test(expected = MappingException::class)
    fun buildDialogEntityStubWithTypePrivateAndParticipantIdsNull_mappingToRemoteDto_MappingException() {
        val entity = buildPrivateDialogEntityStub()
        entity.setParticipantIds(null)
        DialogMapper.dtoRemoteFrom(entity)
    }

    @Test(expected = MappingException::class)
    fun buildDialogEntityStubWithTypePrivateAndParticipantIdsEmpty_mappingToRemoteDto_MappingException() {
        val entity = buildGroupDialogEntityStub()
        entity.setDialogType(PRIVATE)
        entity.setParticipantIds(arrayListOf())
        DialogMapper.dtoRemoteFrom(entity)
    }

    @Test
    fun buildRemoteDialogDTO_mappingToEntity_NoException() {
        val dto = buildRemoteDialogDTOStub()
        val entity = DialogMapper.toEntity(dto)

        assertEquals(dto.id, entity.getDialogId())
        assertEquals(dto.ownerId, entity.getOwnerId())
        assertEquals(dto.type, entity.getType()?.code)
        assertEquals(dto.participantIds, entity.getParticipantIds())
        assertEquals(dto.updatedAt, entity.getUpdatedAt())
        assertEquals(dto.lastMessageText, entity.getLastMessage()?.getContent())
        assertEquals(dto.lastMessageDateSent, entity.getLastMessage()?.getTime())
        assertEquals(dto.ownerId, entity.getLastMessage()?.getSenderId())
        assertEquals(dto.unreadMessageCount, entity.getUnreadMessagesCount())
        assertEquals(dto.name, entity.getName())
        assertEquals(dto.photo, entity.getPhoto())
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypeNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        dto.type = null
        DialogMapper.toEntity(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePublicAndNameNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val publicType = 1
        dto.type = publicType
        dto.name = null
        DialogMapper.toEntity(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypeGroupAndNameEmpty_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val groupType = 2
        dto.type = groupType
        dto.name = ""
        DialogMapper.toEntity(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePrivateAndParticipantIdsNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val privateType = 3
        dto.type = privateType
        dto.participantIds = null
        DialogMapper.toEntity(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePrivateAndParticipantIdsEmpty_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val privateType = 3
        dto.type = privateType
        dto.participantIds = arrayListOf()
        DialogMapper.toEntity(dto)
    }

    private fun buildRemoteDialogDTOStub(): RemoteDialogDTO {
        val dto = RemoteDialogDTO()
        dto.id = UUID.randomUUID().toString()

        dto.type = Random.nextInt(1, 3)
        dto.ownerId = Random.nextInt(0, 1000)

        val participantIds = arrayListOf(123, 234, 345)
        dto.participantIds = participantIds

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        dto.updatedAt = currentTime
        dto.lastMessageText = "Last Message Tex ${Random.nextInt(0, 1000)}"

        val randomLong = Random.nextLong(0, Long.MAX_VALUE)
        dto.lastMessageDateSent = randomLong
        dto.lastMessageUserId = Random.nextInt(0, 1000)
        dto.unreadMessageCount = Random.nextInt(0, 100)
        dto.name = "Dialog Stub ${Random.nextInt(0, 1000)}"
        dto.photo = "URL ${Random.nextInt(0, 1000)}"
        return dto
    }

    private fun buildGroupDialogEntityStub(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogId(UUID.randomUUID().toString())
        entity.setDialogType(GROUP)
        entity.setOwnerId(Random.nextInt(0, 1000))

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        entity.setUpdatedAt(currentTime)

        val lastMessageEntity = buildsDialogEntityStub()
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(Random.nextInt(0, 10))
        entity.setName("Dialog Stub ${Random.nextInt(0, 1000)}")
        entity.setPhoto(" ${Random.nextInt(0, 1000)}")
        return entity
    }

    private fun buildPrivateDialogEntityStub(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogId(UUID.randomUUID().toString())
        entity.setDialogType(PRIVATE)
        entity.setOwnerId(Random.nextInt(0, 1000))

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        entity.setUpdatedAt(currentTime)

        val lastMessageEntity = buildsDialogEntityStub()
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(Random.nextInt(0, 10))
        entity.setName("Dialog Stub ${Random.nextInt(0, 1000)}")
        entity.setPhoto(" ${Random.nextInt(0, 1000)}")
        return entity
    }

    private fun buildPublicDialogEntityStub(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogId(UUID.randomUUID().toString())
        entity.setDialogType(PUBLIC)
        entity.setOwnerId(Random.nextInt(0, 1000))

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        entity.setUpdatedAt(currentTime)

        val lastMessageEntity = buildsDialogEntityStub()
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(Random.nextInt(0, 10))
        entity.setName("Dialog Stub ${Random.nextInt(0, 1000)}")
        entity.setPhoto(" ${Random.nextInt(0, 1000)}")
        return entity
    }

    private fun buildsDialogEntityStub(): IncomingChatMessageEntity {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setContent("Last Message Text ${Random.nextInt(0, 1000)}")
        val randomLong = Random.nextLong(0, Long.MAX_VALUE)
        entity.setTime(randomLong)
        entity.setSenderId(Random.nextInt(0, 1000))
        return entity
    }
}