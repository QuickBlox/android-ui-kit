/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.ai

import com.quickblox.android_ui_kit.data.repository.mapper.AIRephraseMapper
import com.quickblox.android_ui_kit.data.repository.mapper.AITranslateMapper
import com.quickblox.android_ui_kit.data.source.ai.AIDataSource
import com.quickblox.android_ui_kit.data.source.exception.AIDataSourceException
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.repository.AIRepository

class AIRepositoryImpl(private val aiDataSource: AIDataSource) : AIRepository {
    override fun translateIncomingMessageByOpenAIToken(messageEntity: IncomingChatMessageEntity): AITranslateIncomingChatMessageEntity {
        try {
            val answerDTO = AITranslateMapper.DTOFrom(messageEntity)
            val responseDTO = aiDataSource.translateIncomingMessageByOpenAIToken(answerDTO)

            return AITranslateMapper.entityFrom(responseDTO, messageEntity)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun translateIncomingMessageByQuickBloxToken(
        messageEntity: IncomingChatMessageEntity,
        token: String,
    ): AITranslateIncomingChatMessageEntity {
        try {
            val answerDTO = AITranslateMapper.DTOFrom(messageEntity)
            val responseDTO = aiDataSource.translateIncomingMessageByQuickBloxToken(answerDTO, token)

            return AITranslateMapper.entityFrom(responseDTO, messageEntity)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun rephraseByOpenAIToken(toneEntity: AIRephraseToneEntity): AIRephraseToneEntity {
        try {
            val requestDTO = AIRephraseMapper.entityToDto(toneEntity)
            val resultDTO = aiDataSource.rephraseByOpenAIToken(requestDTO)
            val resultEntity = AIRephraseMapper.dtoToEntity(resultDTO)
            return resultEntity
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun rephraseByQuickBloxToken(toneEntity: AIRephraseToneEntity, token: String): AIRephraseToneEntity {
        try {
            val requestDTO = AIRephraseMapper.entityToDto(toneEntity)
            val resultDTO = aiDataSource.rephraseByQuickBloxToken(requestDTO, token)
            val resultEntity = AIRephraseMapper.dtoToEntity(resultDTO)
            return resultEntity
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun getAllRephraseTones(): List<AIRephraseToneEntity> {
        try {
            val results = aiDataSource.getAllRephraseTones()
            return AIRephraseMapper.dtosToEntities(results)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }
}