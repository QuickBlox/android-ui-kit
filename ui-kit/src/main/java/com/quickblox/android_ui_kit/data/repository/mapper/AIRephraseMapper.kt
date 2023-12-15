/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import android.text.TextUtils
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseMessageDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseToneDTO
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseToneEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

@ExcludeFromCoverage
object AIRephraseMapper {
    fun dtosToToneEntities(dtos: List<AIRephraseToneDTO>): MutableList<AIRephraseToneEntity> {
        val entities = mutableListOf<AIRephraseToneEntity>()

        dtos.forEach { dto ->
            val entity = dtoToToneEntity(dto)
            entities.add(entity)
        }

        return entities
    }

    fun toneEntitiesToDtos(entities: List<AIRephraseToneEntity>): List<AIRephraseToneDTO> {
        val dtos = mutableListOf<AIRephraseToneDTO>()

        entities.forEach { entity ->
            val dto = toneEntityToDTOs(entity)
            dtos.add(dto)
        }

        return dtos
    }

    fun dtoToToneEntity(dto: AIRephraseToneDTO): AIRephraseToneEntity {
        val toneName = dto.toneName
        if (TextUtils.isEmpty(toneName)) {
            throw MappingException("toneName should not be blank")
        }

        val smileCode = dto.icon
        val description = dto.descriptionTone
        val tone = AIRephraseToneEntityImpl(toneName, description, smileCode)

        return tone
    }

    fun toneEntityToDTOs(entity: AIRephraseToneEntity): AIRephraseToneDTO {
        val toneName = entity.getName()
        if (TextUtils.isEmpty(toneName)) {
            throw MappingException("toneName should not be blank")
        }
        val dto = AIRephraseToneDTO()
        dto.toneName = toneName
        dto.descriptionTone = entity.getDescription()
        dto.icon = entity.getIcon()
        return dto
    }

    fun dtoToEntity(dto: AIRephraseDTO): AIRephraseEntity {
        val toneName = dto.tone.toneName
        if (TextUtils.isEmpty(toneName)) {
            throw MappingException("toneName should not be blank")
        }

        val smileCode = dto.tone.icon
        val description = dto.tone.descriptionTone
        val tone = AIRephraseToneEntityImpl(toneName, description, smileCode)
        val entity = AIRephraseEntityImpl(tone)
        entity.setRephrasedText(dto.rephrasedText)
        entity.setOriginalText(dto.originalText)

        return entity
    }

    fun entityToDto(rephraseEntity: AIRephraseEntity): AIRephraseDTO {
        val toneDTO = AIRephraseToneDTO()
        toneDTO.toneName = rephraseEntity.getRephraseTone().getName()
        toneDTO.icon = rephraseEntity.getRephraseTone().getIcon()
        toneDTO.descriptionTone = rephraseEntity.getRephraseTone().getDescription()

        val dto = AIRephraseDTO(toneDTO)
        dto.rephrasedText = rephraseEntity.getRephrasedText()
        dto.originalText = rephraseEntity.getOriginalText()

        return dto
    }


    fun messageToDto(message: MessageEntity): AIRephraseMessageDTO {
        val dto = AIRephraseMessageDTO()
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

    fun messagesToDtos(messagesFromUIKit: List<MessageEntity>): List<AIRephraseMessageDTO> {
        val dtos = mutableListOf<AIRephraseMessageDTO>()

        messagesFromUIKit.forEach { messageEntity ->
            val dto = messageToDto(messageEntity)
            dtos.add(dto)
        }
        return dtos
    }
}