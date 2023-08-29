/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateDTO
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity

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
        translateMessage.setTranslations(dto.translations)
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
}