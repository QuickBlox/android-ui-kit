/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import android.text.TextUtils
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseToneEntityImpl
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

@ExcludeFromCoverage
object AIRephraseMapper {
    fun dtosToEntities(dtos: List<AIRephraseDTO>): List<AIRephraseToneEntity> {
        val entities = mutableListOf<AIRephraseToneEntity>()

        dtos.forEach { dto ->
            val entity = dtoToEntity(dto)
            entities.add(entity)
        }

        return entities
    }

    fun dtoToEntity(dto: AIRephraseDTO): AIRephraseToneEntity {
        val toneName = dto.toneName
        if (TextUtils.isEmpty(toneName)) {
            throw MappingException("toneName should not be blank")
        }

        val smileCode = dto.smileCode
        val entity = AIRephraseToneEntityImpl(toneName, smileCode)
        entity.setRephrasedText(dto.rephrasedText)
        entity.setOriginalText(dto.originalText)

        return entity
    }

    fun entityToDto(rephraseEntity: AIRephraseToneEntity): AIRephraseDTO {
        val dto = AIRephraseDTO()
        dto.toneName = rephraseEntity.getName()
        dto.smileCode = rephraseEntity.getSmileCode()
        dto.rephrasedText = rephraseEntity.getRephrasedText()
        dto.originalText = rephraseEntity.getOriginalText()

        return dto
    }
}