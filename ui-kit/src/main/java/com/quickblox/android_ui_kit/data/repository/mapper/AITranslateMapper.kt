/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateDTO
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateMessageDTO
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity

@ExcludeFromCoverage
object AITranslateMapper {
    fun DTOFrom(messageEntity: IncomingChatMessageEntity): AITranslateDTO {
        val dto = AITranslateDTO()
        dto.content = messageEntity.getContent()
        return dto
    }

    fun entityFrom(
        dto: AITranslateDTO,
        incomingMessage: IncomingChatMessageEntity,
    ): AITranslateIncomingChatMessageEntity {
        val translateMessage = AITranslateIncomingChatMessageEntity(incomingMessage.getContentType())
        translateMessage.setTranslation(dto.translation)
        translateMessage.setDialogId(incomingMessage.getDialogId())
        translateMessage.setMessageId(incomingMessage.getMessageId())
        translateMessage.setTime(incomingMessage.getTime())
        translateMessage.setContent(incomingMessage.getContent())
        translateMessage.setSenderId(incomingMessage.getSenderId())
        translateMessage.setParticipantId(incomingMessage.getParticipantId())
        translateMessage.setLoggedUserId(incomingMessage.getLoggedUserId())
        translateMessage.setReadIds(incomingMessage.getReadIds())
        translateMessage.setDeliveredIds(incomingMessage.getDeliveredIds())
        translateMessage.setMediaContent(incomingMessage.getMediaContent())
        translateMessage.setSender(incomingMessage.getSender())

        return translateMessage
    }

    fun messagesToDtos(messagesFromUIKit: List<MessageEntity>): List<AITranslateMessageDTO> {
        val dtos = mutableListOf<AITranslateMessageDTO>()

        messagesFromUIKit.forEach { messageEntity ->
            val dto = messageToDto(messageEntity)
            dtos.add(dto)
        }
        return dtos
    }

    fun messageToDto(message: MessageEntity): AITranslateMessageDTO {
        val dto = AITranslateMessageDTO()
        if (message is IncomingChatMessageEntity) {
            message.getContent()?.let {
                dto.isIncomeMessage = true
                dto.text = it
            }
            return dto
        }
        if (message is OutgoingChatMessageEntity) {
            message.getContent()?.let {
                dto.isIncomeMessage = false
                dto.text = it
            }
        }
        return dto
    }
}