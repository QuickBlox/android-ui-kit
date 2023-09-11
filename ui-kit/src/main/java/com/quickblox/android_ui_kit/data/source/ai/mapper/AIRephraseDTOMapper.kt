/*
 * Created by Injoit on 22.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai.mapper

import com.quickblox.android_ai_editing_assistant.model.QBAIRephraseTone
import com.quickblox.android_ai_editing_assistant.model.QBAIRephraseToneImpl
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO

object AIRephraseDTOMapper {
    fun dtoToDtoWithRephrasedText(dto: AIRephraseDTO, rephrasedText: String): AIRephraseDTO {
        val dtoWithRephrasedText = AIRephraseDTO()
        dtoWithRephrasedText.originalText = dto.originalText
        dtoWithRephrasedText.rephrasedText = rephrasedText
        dtoWithRephrasedText.toneName = dto.toneName
        dtoWithRephrasedText.smileCode = dto.smileCode

        return dtoWithRephrasedText
    }

    fun tonesToDtos(tones: List<QBAIRephraseTone>): List<AIRephraseDTO> {
        val dtos = mutableListOf<AIRephraseDTO>()

        tones.forEach { tone ->
            val dto = toneToDto(tone)
            dtos.add(dto)
        }

        return dtos
    }

    fun toneToDto(tone: QBAIRephraseTone): AIRephraseDTO {
        val dto = AIRephraseDTO()
        dto.toneName = tone.getName()
        dto.smileCode = tone.getSmileCode()

        return dto
    }

    fun dtoToTone(dto: AIRephraseDTO): QBAIRephraseTone {
        val toneName = dto.toneName
        val smileCode = dto.smileCode

        return QBAIRephraseToneImpl(toneName, smileCode)
    }
}