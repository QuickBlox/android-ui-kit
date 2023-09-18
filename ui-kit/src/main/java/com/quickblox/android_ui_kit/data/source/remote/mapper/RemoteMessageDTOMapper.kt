/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote.mapper

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile

object RemoteMessageDTOMapper {
    fun messageDTOFrom(qbChatMessage: QBChatMessage, loggedUserId: Int): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = qbChatMessage.id
        dto.dialogId = qbChatMessage.dialogId
        dto.text = qbChatMessage.body ?: ""
        dto.outgoing = qbChatMessage.senderId == loggedUserId
        dto.senderId = qbChatMessage.senderId
        dto.type = parseType(qbChatMessage)
        dto.time = qbChatMessage.dateSent
        dto.participantId = qbChatMessage.recipientId
        dto.loggedUserId = loggedUserId
        dto.readIds = qbChatMessage.readIds
        dto.deliveredIds = qbChatMessage.deliveredIds

        val messageHasAttachment = isChatMessageTypeIn(qbChatMessage) && isExistAttachmentIn(qbChatMessage)
        if (messageHasAttachment) {
            val attachment = qbChatMessage.attachments.toList()[0]
            dto.fileName = attachment.name

            if (qbChatMessage.body == null) {
                dto.fileUrl = attachment.url
            } else {
                dto.fileUrl = parseBody(qbChatMessage.body)
            }

            dto.mimeType = attachment.contentType ?: attachment.type
        }

        if (dto.outgoing == true) {
            dto.outgoingState = parseOutgoingState(qbChatMessage, loggedUserId)
        }

        return dto
    }

    private fun parseBody(body: String?): String? {
        return try {
            val splitUrl = body?.split("|")
            val uid = splitUrl?.get(2)
            QBFile.getPrivateUrlForUID(uid)
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
    }

    private fun parseOutgoingState(
        qbChatMessage: QBChatMessage,
        loggedUserId: Int,
    ): RemoteMessageDTO.OutgoingMessageStates {
        val readIds = qbChatMessage.readIds
        val deliveredIds = qbChatMessage.deliveredIds

        readIds.remove(loggedUserId)
        deliveredIds.remove(loggedUserId)

        val isAlreadyRead = readIds.isNotEmpty()
        if (isAlreadyRead) {
            return RemoteMessageDTO.OutgoingMessageStates.READ
        }

        val isAlreadyDelivered = deliveredIds.isNotEmpty()
        if (isAlreadyDelivered) {
            return RemoteMessageDTO.OutgoingMessageStates.DELIVERED
        }

        return RemoteMessageDTO.OutgoingMessageStates.SENT
    }

    @VisibleForTesting
    fun isExistAttachmentIn(qbChatMessage: QBChatMessage): Boolean {
        return qbChatMessage.attachments != null && qbChatMessage.attachments.isNotEmpty()
    }

    @VisibleForTesting
    fun isChatMessageTypeIn(qbChatMessage: QBChatMessage): Boolean {
        return parseType(qbChatMessage) == RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
    }

    private fun parseType(qbChatMessage: QBChatMessage): RemoteMessageDTO.MessageTypes? {
        if (EventMessageParser.isNotEventFrom(qbChatMessage)) {
            return RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        }

        if (EventMessageParser.isCreatedDialogEventFrom(qbChatMessage)) {
            return RemoteMessageDTO.MessageTypes.EVENT_CREATED_DIALOG
        }

        if (EventMessageParser.isAddedUserEventFrom(qbChatMessage)) {
            return RemoteMessageDTO.MessageTypes.EVENT_ADDED_USER
        }

        if (EventMessageParser.isRemovedUserEventFrom(qbChatMessage)) {
            return RemoteMessageDTO.MessageTypes.EVENT_REMOVED_USER
        }

        if (EventMessageParser.isLeftUserEventFrom(qbChatMessage)) {
            return RemoteMessageDTO.MessageTypes.EVENT_LEFT_USER
        }

        return null
    }

    private fun generateDateSentTime(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun qbChatMessageFrom(dto: RemoteMessageDTO): QBChatMessage {
        val qbChatMessage = QBChatMessage()

        if (dto.id?.isNotBlank() == true) {
            qbChatMessage.id = dto.id
        }

        qbChatMessage.dialogId = dto.dialogId
        qbChatMessage.body = dto.text
        qbChatMessage.senderId = dto.senderId
        qbChatMessage.recipientId = dto.participantId
        qbChatMessage.setSaveToHistory(true)

        if (isAvailableAttachmentIn(dto)) {
            val attachment = QBAttachment("")
            attachment.name = dto.fileName
            attachment.url = dto.fileUrl
            attachment.contentType = dto.mimeType

            qbChatMessage.addAttachment(attachment)
        }

        dto.time?.let { time ->
            qbChatMessage.dateSent = time
        }

        return qbChatMessage
    }

    private fun isAvailableAttachmentIn(dto: RemoteMessageDTO): Boolean {
        val availableContentType = dto.mimeType?.isNotEmpty() == true
        val availableUrl = dto.fileUrl?.isNotEmpty() == true

        return availableContentType && availableUrl
    }

    fun qbSystemMessageFrom(dto: RemoteMessageDTO): QBChatMessage {
        var qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dto.dialogId
        qbChatMessage.body = dto.text
        qbChatMessage.senderId = dto.senderId
        qbChatMessage.recipientId = dto.participantId
        qbChatMessage.setSaveToHistory(false)

        dto.time?.let { time ->
            qbChatMessage.dateSent = time
        }

        val needAddEventProperties = dto.type != RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        if (needAddEventProperties) {
            qbChatMessage = addEventProperties(dto.type, qbChatMessage)
        }

        return qbChatMessage
    }

    private fun addEventProperties(type: RemoteMessageDTO.MessageTypes?, qbChatMessage: QBChatMessage): QBChatMessage {
        when (type) {
            RemoteMessageDTO.MessageTypes.EVENT_CREATED_DIALOG -> {
                return EventMessageParser.addCreatedDialogPropertyTo(qbChatMessage)
            }
            RemoteMessageDTO.MessageTypes.EVENT_ADDED_USER -> {
                return EventMessageParser.addAddedUsersPropertyTo(qbChatMessage)
            }
            RemoteMessageDTO.MessageTypes.EVENT_REMOVED_USER -> {
                return EventMessageParser.addRemovedUsersPropertyTo(qbChatMessage)
            }
            RemoteMessageDTO.MessageTypes.EVENT_LEFT_USER -> {
                return EventMessageParser.addLeftUsersPropertyTo(qbChatMessage)
            }
            else -> {}
        }

        return qbChatMessage
    }

    fun messageDTOWithDeliveredStatus(messageId: String, dialogId: String, senderId: Int): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = messageId
        dto.dialogId = dialogId
        dto.outgoing = true
        dto.senderId = senderId
        dto.type = RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        dto.time = generateDateSentTime()
        dto.outgoingState = RemoteMessageDTO.OutgoingMessageStates.DELIVERED

        return dto
    }

    fun messageDTOWithReadStatus(messageId: String, dialogId: String, senderId: Int): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = messageId
        dto.dialogId = dialogId
        dto.outgoing = true
        dto.senderId = senderId
        dto.type = RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        dto.time = generateDateSentTime()
        dto.outgoingState = RemoteMessageDTO.OutgoingMessageStates.READ

        return dto
    }
}