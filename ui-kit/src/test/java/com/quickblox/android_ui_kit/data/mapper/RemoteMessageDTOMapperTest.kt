/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.mapper

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteMessageDTOMapper
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatMessage
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class RemoteMessageDTOMapperTest {
    @Test
    fun dtoWithContent_dtoToQBChatMessage_qbChatMessageHasAttachment() {
        val dto = buildRemoteMessageDTO()
        dto.fileUrl = "https://test.com/a.mp3"
        dto.mimeType = "audio/mpeg"

        val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(dto)
        assertTrue(qbChatMessage.attachments.isNotEmpty())
    }

    @Test
    fun dtoWithoutContent_dtoToQBChatMessage_qbChatMessageHasAttachment() {
        val dto = buildRemoteMessageDTO()
        val qbChatMessage = RemoteMessageDTOMapper.qbChatMessageFrom(dto)
        assertTrue(qbChatMessage.attachments == null)
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

    @Test
    fun qbChatMessageHasAttachment_qbChatMessageToDTO_dtoHasContent() {
        val qbAttachment = QBAttachment("")

        val url = "https://test.com/a.mp3"
        qbAttachment.url = url

        val contentType = "audio/mpeg"
        qbAttachment.contentType = contentType

        val qbMessage = QBChatMessage()
        qbMessage.addAttachment(qbAttachment)

        val messageDTO = RemoteMessageDTOMapper.messageDTOFrom(qbMessage, 1000)

        assertEquals(url, messageDTO.fileUrl)
        assertEquals(contentType, messageDTO.mimeType)
    }

    @Test
    fun qbChatMessageHasAttachment_isExistAttachment_receivedTrue() {
        val qbAttachment = QBAttachment("")
        qbAttachment.url = "https://test.com/a.mp3"
        qbAttachment.contentType = "audio/mpeg"

        val qbMessage = QBChatMessage()
        qbMessage.addAttachment(qbAttachment)

        val existAttachment = RemoteMessageDTOMapper.isExistAttachmentIn(qbMessage)
        assertTrue(existAttachment)
    }

    @Test
    fun qbChatMessageNotHasAttachment_isExistAttachment_receivedFalse() {
        val qbMessage = QBChatMessage()
        val existAttachment = RemoteMessageDTOMapper.isExistAttachmentIn(qbMessage)
        assertFalse(existAttachment)
    }

    @Test
    fun qbChatMessageHasCreatedDialogType_isChatMessageType_receivedFalse() {
        val qbMessage = QBChatMessage()
        qbMessage.body = "text"
        qbMessage.setProperty(
            EventMessageParser.PROPERTY_NOTIFICATION_TYPE,
            EventMessageParser.EventTypes.CREATED_DIALOG.value
        )

        val existAttachment = RemoteMessageDTOMapper.isChatMessageTypeIn(qbMessage)
        assertFalse(existAttachment)
    }

    @Test
    fun qbChatMessageHasRemovedUserType_isChatMessageType_receivedFalse() {
        val qbMessage = QBChatMessage()
        qbMessage.body = "text"
        qbMessage.setProperty(
            EventMessageParser.PROPERTY_NOTIFICATION_TYPE,
            EventMessageParser.EventTypes.REMOVED_USER.value
        )

        val existAttachment = RemoteMessageDTOMapper.isChatMessageTypeIn(qbMessage)
        assertFalse(existAttachment)
    }

    @Test
    fun qbChatMessageHasLeftUserType_isChatMessageType_receivedFalse() {
        val qbMessage = QBChatMessage()
        qbMessage.body = "text"
        qbMessage.setProperty(
            EventMessageParser.PROPERTY_NOTIFICATION_TYPE,
            EventMessageParser.EventTypes.LEFT_USER.value
        )

        val existAttachment = RemoteMessageDTOMapper.isChatMessageTypeIn(qbMessage)
        assertFalse(existAttachment)
    }

    @Test
    fun qbChatMessageHasAddedUserType_isChatMessageType_receivedFalse() {
        val qbMessage = QBChatMessage()
        qbMessage.body = "text"
        qbMessage.setProperty(
            EventMessageParser.PROPERTY_NOTIFICATION_TYPE,
            EventMessageParser.EventTypes.ADDED_USER.value
        )

        val existAttachment = RemoteMessageDTOMapper.isChatMessageTypeIn(qbMessage)
        assertFalse(existAttachment)
    }

    @Test
    fun qbChatMessageHasChatType_isChatMessageType_receivedFalse() {
        val qbMessage = QBChatMessage()
        qbMessage.body = "text"

        val existAttachment = RemoteMessageDTOMapper.isChatMessageTypeIn(qbMessage)
        assertTrue(existAttachment)
    }

    @Test
    fun haveCorrectData_messageDTOWithDeliveredStatus_receivedDTO() {
        val messageId = System.currentTimeMillis().toString()
        val dialogId = System.currentTimeMillis().toString()
        val senderId = Random.nextInt(1000, 10000)

        val messageDTO = RemoteMessageDTOMapper.messageDTOWithDeliveredStatus(messageId, dialogId, senderId)

        assertTrue(messageDTO.outgoing!!)
        assertEquals(messageId, messageDTO.id)
        assertEquals(dialogId, messageDTO.dialogId)
        assertEquals(senderId, messageDTO.senderId)
        assertEquals(RemoteMessageDTO.OutgoingMessageStates.DELIVERED, messageDTO.outgoingState)
    }

    @Test
    fun haveCorrectData_messageDTOWithReadStatus_receivedDTO() {
        val messageId = System.currentTimeMillis().toString()
        val dialogId = System.currentTimeMillis().toString()
        val senderId = Random.nextInt(1000, 10000)

        val messageDTO = RemoteMessageDTOMapper.messageDTOWithReadStatus(messageId, dialogId, senderId)

        assertTrue(messageDTO.outgoing!!)
        assertEquals(messageId, messageDTO.id)
        assertEquals(dialogId, messageDTO.dialogId)
        assertEquals(senderId, messageDTO.senderId)
        assertEquals(RemoteMessageDTO.OutgoingMessageStates.READ, messageDTO.outgoingState)
    }
}