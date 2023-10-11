/*
 * Created by Injoit on 22.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai.mapper

import com.quickblox.android_ai_editing_assistant.message.MeMessage
import com.quickblox.android_ai_editing_assistant.message.Message
import com.quickblox.android_ai_editing_assistant.message.OtherMessage
import com.quickblox.android_ai_editing_assistant.model.Tone
import com.quickblox.android_ai_editing_assistant.model.ToneImpl
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseMessageDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseToneDTO

object AIRephraseDTOMapper {
    fun dtoToDtoWithRephrasedText(dto: AIRephraseDTO, rephrasedText: String): AIRephraseDTO {
        val toneDTO = AIRephraseToneDTO()
        toneDTO.toneName = dto.tone.toneName
        toneDTO.descriptionTone = dto.tone.descriptionTone
        toneDTO.icon = dto.tone.icon

        val dtoWithRephrasedText = AIRephraseDTO(toneDTO)
        dtoWithRephrasedText.originalText = dto.originalText
        dtoWithRephrasedText.rephrasedText = rephrasedText


        return dtoWithRephrasedText
    }

    fun tonesToDtos(tones: List<Tone>): List<AIRephraseToneDTO> {
        val dtos = mutableListOf<AIRephraseToneDTO>()

        tones.forEach { tone ->
            val dto = toneToDto(tone)
            dtos.add(dto)
        }

        return dtos
    }

    fun dtosToTones(dtos: List<AIRephraseToneDTO>): List<Tone> {
        val tones = mutableListOf<Tone>()

        dtos.forEach { dto ->
            val tone = dtoToTone(dto)
            tones.add(tone)
        }

        return tones
    }

    fun toneToDto(tone: Tone): AIRephraseToneDTO {
        val toneDTO = AIRephraseToneDTO()
        toneDTO.toneName = tone.getName()
        toneDTO.descriptionTone = tone.getDescription()
        toneDTO.icon = tone.getIcon()

        return toneDTO
    }

    fun dtoToTone(dto: AIRephraseToneDTO): Tone {
        val toneName = dto.toneName
        val icon = dto.icon
        val descriptionTone = dto.descriptionTone

        return ToneImpl(toneName, descriptionTone, icon)
    }

    fun dtoToMessage(dto: AIRephraseMessageDTO): Message {
        return if (dto.isIncomeMessage) {
            OtherMessage(dto.text)
        } else {
            MeMessage(dto.text)
        }
    }

    fun dtosToMessages(dtos: List<AIRephraseMessageDTO>): List<Message> {
        val messages = mutableListOf<Message>()
        dtos.forEach {
            val message = dtoToMessage(it)
            messages.add(message)
        }
        return messages
    }
}