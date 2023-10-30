/*
 * Created by Injoit on 13.10.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.ai.mapper


import com.quickblox.android_ai_translate.message.MeMessage
import com.quickblox.android_ai_translate.message.Message
import com.quickblox.android_ai_translate.message.OtherMessage
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateMessageDTO


object AITranslateDTOMapper {
    fun dtoToMessage(dto: AITranslateMessageDTO): Message {
        return if (dto.isIncomeMessage) {
            OtherMessage(dto.text)
        } else {
            MeMessage(dto.text)
        }
    }

    fun dtosToMessages(dtos: List<AITranslateMessageDTO>): List<Message> {
        val messages = mutableListOf<Message>()
        dtos.forEach {
            val message = dtoToMessage(it)
            messages.add(message)
        }
        return messages
    }
}