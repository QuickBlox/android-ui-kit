/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.parser.ForwardReplyMessageParser
import com.quickblox.android_ui_kit.domain.entity.implementation.message.EventMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity.EventTypes
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

object MessageMapper {
    fun incomingChatEntityFrom(dto: RemoteMessageDTO): IncomingChatMessageEntity {
        val contentType = getContentTypeFrom(dto)
        val entity: IncomingChatMessageEntity = IncomingChatMessageEntityImpl(contentType)

        entity.setDialogId(dto.dialogId)
        entity.setMessageId(dto.id)
        entity.setTime(dto.time)
        entity.setContent(dto.text)
        entity.setSenderId(dto.senderId)
        entity.setParticipantId(dto.participantId)
        entity.setLoggedUserId(dto.loggedUserId)
        entity.setReadIds(dto.readIds)
        entity.setDeliveredIds(dto.deliveredIds)

        if (isExistFileIn(dto)) {
            val mediaContent = getMediaContentFrom(dto)
            entity.setMediaContent(mediaContent)
        }

        if (dto.isForwardedOrReplied == true) {
            val parsedForwardedRepliedType = ForwardReplyMessageParser.parseForwardRepliedTypeFrom(dto)
            entity.setForwardOrReplied(parsedForwardedRepliedType)

            val parsedMessages = ForwardReplyMessageParser.parseIncomingMessagesFrom(dto)
            entity.setForwardedRepliedMessages(parsedMessages)
        }

        return entity
    }

    fun outgoingChatEntityFrom(dto: RemoteMessageDTO): OutgoingChatMessageEntity {
        val outgoingState = parseStateFrom(dto.outgoingState)
        val contentType = getContentTypeFrom(dto)
        val entity: OutgoingChatMessageEntity = OutgoingChatMessageEntityImpl(outgoingState, contentType)

        entity.setDialogId(dto.dialogId)
        entity.setMessageId(dto.id)
        entity.setTime(dto.time)
        entity.setSenderId(dto.senderId)
        entity.setContent(dto.text)
        entity.setParticipantId(dto.participantId)

        if (isExistFileIn(dto)) {
            val mediaContent = getMediaContentFrom(dto)
            entity.setMediaContent(mediaContent)
        }

        if (dto.isForwardedOrReplied == true) {
            val parsedForwardedRepliedType = ForwardReplyMessageParser.parseForwardRepliedTypeFrom(dto)
            entity.setForwardOrReplied(parsedForwardedRepliedType)

            val parsedMessages = ForwardReplyMessageParser.parseOutgoingMessagesFrom(dto)
            entity.setForwardedRepliedMessages(parsedMessages)

            var content = dto.text
            if (entity.isForwarded() && content.isNullOrBlank()) {
                content = "[Forwarded_Message]"
                entity.setContent(content)
            }
        }

        return entity
    }

    private fun parseStateFrom(state: RemoteMessageDTO.OutgoingMessageStates?): OutgoingChatMessageEntity.OutgoingStates? {
        when (state) {
            RemoteMessageDTO.OutgoingMessageStates.DELIVERED -> {
                return OutgoingChatMessageEntity.OutgoingStates.DELIVERED
            }

            RemoteMessageDTO.OutgoingMessageStates.READ -> {
                return OutgoingChatMessageEntity.OutgoingStates.READ
            }

            RemoteMessageDTO.OutgoingMessageStates.SENDING -> {
                return OutgoingChatMessageEntity.OutgoingStates.SENDING
            }

            RemoteMessageDTO.OutgoingMessageStates.SENT -> {
                return OutgoingChatMessageEntity.OutgoingStates.SENT
            }

            else -> {
                return null
            }
        }
    }

    @VisibleForTesting
    fun getContentTypeFrom(dto: RemoteMessageDTO): ChatMessageEntity.ContentTypes {
        val isText = !isExistFileIn(dto)
        if (isText) {
            return ChatMessageEntity.ContentTypes.TEXT
        } else {
            return ChatMessageEntity.ContentTypes.MEDIA
        }
    }

    @VisibleForTesting
    fun isExistFileIn(dto: RemoteMessageDTO): Boolean {
        val isAvailableFileName = dto.fileName?.isNotEmpty() == true
        val isAvailableContentType = dto.mimeType?.isNotEmpty() == true
        return isAvailableFileName && isAvailableContentType
    }

    @VisibleForTesting
    fun getMediaContentFrom(dto: RemoteMessageDTO): MediaContentEntity {
        val file = MediaContentEntityImpl(dto.fileName!!, dto.fileUrl!!, dto.mimeType!!)
        return file
    }

    fun eventEntityFrom(dto: RemoteMessageDTO): EventMessageEntity {
        val entity: EventMessageEntity = EventMessageEntityImpl()

        entity.setDialogId(dto.dialogId)
        entity.setMessageId(dto.id)
        entity.setText(dto.text)
        entity.setTime(dto.time)
        entity.setParticipantId(dto.participantId)
        entity.setSenderId(dto.senderId)
        entity.setLoggedUserId(dto.loggedUserId)
        entity.setReadIds(dto.readIds)
        entity.setDeliveredIds(dto.deliveredIds)

        val parsedEntityType = parseEventEntityTypeFrom(dto.type)
        entity.setEventType(parsedEntityType)

        return entity
    }

    private fun parseEventEntityTypeFrom(type: RemoteMessageDTO.MessageTypes?): EventTypes? {
        var parsedEntityType: EventTypes? = null

        when (type) {
            RemoteMessageDTO.MessageTypes.EVENT_CREATED_DIALOG -> {
                parsedEntityType = EventTypes.CREATED_DIALOG
            }

            RemoteMessageDTO.MessageTypes.EVENT_ADDED_USER -> {
                parsedEntityType = EventTypes.ADDED_USER_TO_DIALOG
            }

            RemoteMessageDTO.MessageTypes.EVENT_LEFT_USER -> {
                parsedEntityType = EventTypes.LEFT_USER_FROM_DIALOG
            }

            RemoteMessageDTO.MessageTypes.EVENT_REMOVED_USER -> {
                parsedEntityType = EventTypes.REMOVED_USER_FROM_DIALOG
            }

            else -> {}
        }

        return parsedEntityType
    }

    fun remoteDTOFromReplyChatMessage(
        replyMessages: ChatMessageEntity, relateMessage: OutgoingChatMessageEntity
    ): RemoteMessageDTO {
        val dto = remoteDTOFromChatMessage(relateMessage)

        dto.properties = ForwardReplyMessageParser.parseReplyPropertiesFrom(replyMessages)
        dto.isForwardedOrReplied = true

        return dto
    }

    fun remoteDTOFromForwardChatMessage(
        forwardMessages: List<ChatMessageEntity>, relateMessage: OutgoingChatMessageEntity
    ): RemoteMessageDTO {
        val dto = remoteDTOFromChatMessage(relateMessage)

        dto.properties = ForwardReplyMessageParser.parseForwardPropertiesFrom(forwardMessages)
        dto.isForwardedOrReplied = true

        return dto
    }

    fun remoteDTOFromChatMessage(entity: ChatMessageEntity): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = entity.getMessageId()
        dto.dialogId = entity.getDialogId()
        dto.text = entity.getContent()
        dto.outgoing = true
        dto.time = entity.getTime()
        dto.participantId = entity.getParticipantId()
        dto.senderId = entity.getSenderId()

        val isMediaContentType = entity.getContentType() == ChatMessageEntity.ContentTypes.MEDIA
        if (isMediaContentType ) {
            dto.fileName = entity.getMediaContent()?.getName()
            dto.mimeType = entity.getMediaContent()?.getMimeType()
            dto.fileUrl = entity.getMediaContent()?.getUrl()
        }

        return dto
    }

    fun remoteDTOFromOutgoingChatMessage(entity: OutgoingChatMessageEntity): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = entity.getMessageId()
        dto.dialogId = entity.getDialogId()
        dto.text = entity.getContent()
        dto.outgoing = true
        dto.time = entity.getTime()
        dto.participantId = entity.getParticipantId()

        val isMediaContentType = entity.getContentType() == ChatMessageEntity.ContentTypes.MEDIA
        if (isMediaContentType) {
            dto.fileName = entity.getMediaContent()?.getName()
            dto.mimeType = entity.getMediaContent()?.getMimeType()
        }

        if (entity.isForwardedOrReplied()) {
            val forwardedMessages = entity.getForwardedRepliedMessages()
            forwardedMessages?.let {
                if (entity.isForwarded()) {
                    dto.properties = ForwardReplyMessageParser.parseForwardPropertiesFrom(forwardedMessages)
                }
                if (entity.isReplied()) {
                    val replyMessage = forwardedMessages[0]
                    dto.properties = ForwardReplyMessageParser.parseReplyPropertiesFrom(replyMessage)
                }
                dto.isForwardedOrReplied = true
            }
        }

        return dto
    }

    fun remoteDTOFromEventMessage(entity: EventMessageEntity): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = entity.getMessageId()
        dto.dialogId = entity.getDialogId()
        dto.text = entity.getText()
        dto.participantId = entity.getParticipantId()
        dto.needSendChatMessage = isNeedSendChatMessage(entity.getEventType())
        dto.type = parseEventMessageTypeFrom(entity.getEventType())

        return dto
    }

    fun remoteDTOFromMessageEntity(entity: MessageEntity): RemoteMessageDTO {
        val dto = RemoteMessageDTO()
        dto.id = entity.getMessageId()
        dto.dialogId = entity.getDialogId()
        dto.senderId = entity.getSenderId()
        dto.time = entity.getTime()
        dto.participantId = entity.getParticipantId()

        return dto
    }

    private fun isNeedSendChatMessage(type: EventTypes?): Boolean {
        return when (type) {
            EventTypes.CREATED_DIALOG -> true
            EventTypes.ADDED_USER_TO_DIALOG -> true
            EventTypes.REMOVED_USER_FROM_DIALOG -> true
            EventTypes.LEFT_USER_FROM_DIALOG -> true

            EventTypes.STARTED_TYPING -> false
            EventTypes.STOPPED_TYPING -> false

            else -> throw MappingException("The EventType should not be null")
        }
    }

    private fun parseEventMessageTypeFrom(type: EventTypes?): RemoteMessageDTO.MessageTypes? {
        var parsedMessageType: RemoteMessageDTO.MessageTypes? = null

        when (type) {
            EventTypes.CREATED_DIALOG -> {
                parsedMessageType = RemoteMessageDTO.MessageTypes.EVENT_CREATED_DIALOG
            }

            EventTypes.ADDED_USER_TO_DIALOG -> {
                parsedMessageType = RemoteMessageDTO.MessageTypes.EVENT_ADDED_USER
            }

            EventTypes.LEFT_USER_FROM_DIALOG -> {
                parsedMessageType = RemoteMessageDTO.MessageTypes.EVENT_LEFT_USER
            }

            EventTypes.REMOVED_USER_FROM_DIALOG -> {
                parsedMessageType = RemoteMessageDTO.MessageTypes.EVENT_REMOVED_USER
            }

            EventTypes.STARTED_TYPING, EventTypes.STOPPED_TYPING -> {
            }

            else -> {
                return null
            }
        }

        return parsedMessageType
    }
}