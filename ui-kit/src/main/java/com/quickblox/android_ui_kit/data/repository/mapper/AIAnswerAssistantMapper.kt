/*
 * Created by Injoit on 18.10.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.ai.AIAnswerAssistantMessageDTO
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity

object AIAnswerAssistantMapper {
    fun messageToDto(message: MessageEntity): AIAnswerAssistantMessageDTO {
        val dto = AIAnswerAssistantMessageDTO()
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

    fun messagesToDtos(messagesFromUIKit: List<MessageEntity>): List<AIAnswerAssistantMessageDTO> {
        val dtos = mutableListOf<AIAnswerAssistantMessageDTO>()

        messagesFromUIKit.forEach { messageEntity ->
            val dto = messageToDto(messageEntity)
            dtos.add(dto)
        }
        return dtos
    }
}