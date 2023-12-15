/*
 * Created by Injoit on 19.10.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.ai.mapper

import com.quickblox.android_ai_answer_assistant.message.MeMessage
import com.quickblox.android_ai_answer_assistant.message.Message
import com.quickblox.android_ai_answer_assistant.message.OtherMessage
import com.quickblox.android_ui_kit.data.dto.ai.AIAnswerAssistantMessageDTO


object AIAnswerAssistantDTOMapper {
    fun dtoToMessage(dto: AIAnswerAssistantMessageDTO): Message {
        return if (dto.isIncomeMessage) {
            OtherMessage(dto.text)
        } else {
            MeMessage(dto.text)
        }
    }

    fun dtosToMessages(dtos: List<AIAnswerAssistantMessageDTO>): List<Message> {
        val messages = mutableListOf<Message>()
        dtos.forEach {
            val message = dtoToMessage(it)
            messages.add(message)
        }
        return messages
    }
}