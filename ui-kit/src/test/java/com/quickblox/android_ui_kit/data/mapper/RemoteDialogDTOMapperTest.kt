/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.mapper

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteDialogDTOMapper
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class RemoteDialogDTOMapperTest {
    @Test
    fun buildRemoteDialogDTO_mappingToQbChatDialog_NoException() {
        val dto = buildRemoteDialogDTOStub()
        val chatDialog = RemoteDialogDTOMapper.toQBDialogFrom(dto)

        assertEquals(dto.id, chatDialog.dialogId)
        assertEquals(dto.ownerId, chatDialog.userId)
        assertEquals(dto.type, chatDialog.type.code)
        assertThat(dto.participantIds, `is`(chatDialog.occupants))

        val updatedAt = RemoteDialogDTOMapper.stringFrom(chatDialog.updatedAt)
        assertEquals(dto.updatedAt, updatedAt)
        assertEquals(dto.lastMessageText, chatDialog.lastMessage)
        assertEquals(dto.lastMessageDateSent, chatDialog.lastMessageDateSent)
        assertEquals(dto.lastMessageUserId, chatDialog.lastMessageUserId)
        assertEquals(dto.unreadMessageCount, chatDialog.unreadMessageCount)
        assertEquals(dto.name, chatDialog.name)
        assertEquals(dto.photo, chatDialog.photo)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypeNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        dto.type = null
        RemoteDialogDTOMapper.toQBDialogFrom(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePublicAndNameNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val publicType = 1
        dto.type = publicType
        dto.name = null
        RemoteDialogDTOMapper.toQBDialogFrom(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypeGroupAndNameEmpty_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val groupType = 2
        dto.type = groupType
        dto.name = ""
        RemoteDialogDTOMapper.toQBDialogFrom(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePrivateAndParticipantIdsNull_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val privateType = 3
        dto.type = privateType
        dto.participantIds = null
        RemoteDialogDTOMapper.toQBDialogFrom(dto)
    }

    @Test(expected = MappingException::class)
    fun buildRemoteDialogDTOWithTypePrivateAndParticipantIdsEmpty_mappingToQbChatDialog_MappingException() {
        val dto = buildRemoteDialogDTOStub()
        val privateType = 3
        dto.type = privateType
        dto.participantIds = arrayListOf()
        RemoteDialogDTOMapper.toQBDialogFrom(dto)
    }

    @Test
    fun buildQbChatDialog_mappingToRemoteDialogDTO_NoException() {
        val chatDialog = buildQBChatDialogStub()
        val dto = RemoteDialogDTOMapper.toDTOFrom(chatDialog, null)

        assertEquals(dto.id, chatDialog.dialogId)
        assertEquals(dto.ownerId, chatDialog.userId)
        assertEquals(dto.type, chatDialog.type.code)
        assertThat(dto.participantIds, `is`(chatDialog.occupants))

        val updatedAt = RemoteDialogDTOMapper.stringFrom(chatDialog.updatedAt)
        assertEquals(dto.updatedAt, updatedAt)
        assertEquals(dto.lastMessageText, chatDialog.lastMessage)
        assertEquals(dto.lastMessageDateSent, chatDialog.lastMessageDateSent)
        assertEquals(dto.lastMessageUserId, chatDialog.lastMessageUserId)
        assertEquals(dto.unreadMessageCount, chatDialog.unreadMessageCount)
        assertEquals(dto.name, chatDialog.name)
        assertEquals(dto.photo, chatDialog.photo)
    }

    @Test(expected = MappingException::class)
    fun buildQbChatDialogWithTypePublicAndNameNull_mappingToRemoteDialogDTO_MappingException() {
        val chatDialog = buildQBChatDialogStub()
        chatDialog.type = QBDialogType.PUBLIC_GROUP
        chatDialog.name = null
        RemoteDialogDTOMapper.toDTOFrom(chatDialog, null)
    }

    @Test(expected = MappingException::class)
    fun buildQbChatDialogWithTypeGroupAndNameEmpty_mappingToRemoteDialogDTO_MappingException() {
        val chatDialog = buildQBChatDialogStub()
        chatDialog.type = QBDialogType.GROUP
        chatDialog.name = ""
        RemoteDialogDTOMapper.toDTOFrom(chatDialog, null)
    }

    @Test(expected = MappingException::class)
    fun buildQbChatDialogWithTypePrivateAndParticipantIdsNull_mappingToRemoteDialogDTO_MappingException() {
        val chatDialog = buildQBChatDialogStub()
        chatDialog.type = QBDialogType.PRIVATE
        chatDialog.setOccupantsIds(null)
        RemoteDialogDTOMapper.toDTOFrom(chatDialog, null)
    }

    @Test(expected = MappingException::class)
    fun buildQbChatDialogWithTypePrivateAndParticipantIdsEmpty_mappingToRemoteDialogDTO_MappingException() {
        val chatDialog = buildQBChatDialogStub()
        chatDialog.type = QBDialogType.PRIVATE
        chatDialog.setOccupantsIds(arrayListOf())
        RemoteDialogDTOMapper.toDTOFrom(chatDialog,null)
    }

    private fun buildRemoteDialogDTOStub(): RemoteDialogDTO {
        val dto = RemoteDialogDTO()
        dto.id = UUID.randomUUID().toString()

        dto.type = nextInt(1, 3)
        dto.ownerId = nextInt(0, 1000)

        val participantIds = arrayListOf(123, 234, 345)
        dto.participantIds = participantIds

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        dto.updatedAt = currentTime
        dto.lastMessageText = "Last Message Tex ${nextInt(0, 1000)}"

        val randomLong = nextLong(0, Long.MAX_VALUE)
        dto.lastMessageDateSent = randomLong
        dto.lastMessageUserId = nextInt(0, 1000)
        dto.unreadMessageCount = nextInt(0, 100)
        dto.name = "Dialog Stub ${nextInt(0, 1000)}"
        dto.photo = "URL ${nextInt(0, 1000)}"
        return dto
    }

    private fun buildQBChatDialogStub(): QBChatDialog {
        val chatDialog = QBChatDialog()
        chatDialog.dialogId = UUID.randomUUID().toString()
        chatDialog.type = QBDialogType.GROUP
        chatDialog.userId = nextInt(0, 100)

        val currentTime = Calendar.getInstance().time
        chatDialog.updatedAt = currentTime
        chatDialog.lastMessage = "Last Message Tex"

        val randomLong = nextLong(0, Long.MAX_VALUE)
        chatDialog.lastMessageDateSent = randomLong

        chatDialog.lastMessageUserId = nextInt(0, 100)
        chatDialog.unreadMessageCount = nextInt(0, 100)
        chatDialog.name = "Dialog Stub"
        chatDialog.photo = "URL"
        return chatDialog
    }
}