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
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
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

    override fun rephraseWithApiKE(
        toneEntity: AIRephraseEntity,
        messagesFromUIKit: List<MessageEntity>,
    ): AIRephraseEntity {
        try {
            val requestDTO = AIRephraseMapper.entityToDto(toneEntity)
            val messagesDTO = AIRephraseMapper.messagesToDtos(messagesFromUIKit)
            val resultDTO = aiDataSource.rephraseWithApiKey(requestDTO, messagesDTO)
            val resultEntity = AIRephraseMapper.dtoToEntity(resultDTO)
            return resultEntity
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun rephraseWithProxyServer(
        toneEntity: AIRephraseEntity,
        token: String,
        messagesFromUIKit: List<MessageEntity>,
    ): AIRephraseEntity {
        try {
            val requestDTO = AIRephraseMapper.entityToDto(toneEntity)
            val messagesDTO = AIRephraseMapper.messagesToDtos(messagesFromUIKit)
            val resultDTO = aiDataSource.rephraseWithProxyServer(requestDTO, token, messagesDTO)
            val resultEntity = AIRephraseMapper.dtoToEntity(resultDTO)
            return resultEntity
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun getAllRephraseTones(): List<AIRephraseToneEntity> {
        try {
            val results = aiDataSource.getAllRephraseTones()
            return AIRephraseMapper.dtosToToneEntities(results)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun setAllRephraseTones(rephraseTones: List<AIRephraseToneEntity>) {
        try {
            val dtos = AIRephraseMapper.toneEntitiesToDtos(rephraseTones)

            aiDataSource.setAllRephraseTones(dtos)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }
}