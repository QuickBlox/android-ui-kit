/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.ai

import com.quickblox.android_ui_kit.data.repository.mapper.AIAnswerAssistantMapper
import com.quickblox.android_ui_kit.data.repository.mapper.AIRephraseMapper
import com.quickblox.android_ui_kit.data.repository.mapper.AITranslateMapper
import com.quickblox.android_ui_kit.data.source.ai.AIDataSource
import com.quickblox.android_ui_kit.data.source.exception.AIDataSourceException
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.repository.AIRepository

class AIRepositoryImpl(private val aiDataSource: AIDataSource) : AIRepository {
    override fun translateIncomingMessageWithApiKey(
        messageEntity: ForwardedRepliedMessageEntity,
        messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity {
        try {
            val translateDTO = AITranslateMapper.DTOFrom(messageEntity)
            val messagesDTO = AITranslateMapper.messagesToDtos(messagesFromUIKit)
            val responseDTO = aiDataSource.translateIncomingMessageWithApiKey(translateDTO, messagesDTO)

            return AITranslateMapper.entityFrom(responseDTO, messageEntity)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun translateIncomingMessageWithProxyServer(
        messageEntity: ForwardedRepliedMessageEntity,
        token: String,
        messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity {
        try {
            val answerDTO = AITranslateMapper.DTOFrom(messageEntity)
            val messagesDTO = AITranslateMapper.messagesToDtos(messagesFromUIKit)
            val responseDTO = aiDataSource.translateIncomingMessageWithProxyServer(answerDTO, token, messagesDTO)

            return AITranslateMapper.entityFrom(responseDTO, messageEntity)
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun createAnswerWithApiKey(messagesFromUIKit: List<MessageEntity>): String {
        try {
            val messagesDTO = AIAnswerAssistantMapper.messagesToDtos(messagesFromUIKit)
            val answer = aiDataSource.createAnswerWithApiKey(messagesDTO)

            return answer
        } catch (exception: AIDataSourceException) {
            throw AIRepositoryException(exception.message ?: "Unexpected Exception")
        }
    }

    override fun createAnswerWithProxyServer(messagesFromUIKit: List<MessageEntity>, token: String): String {
        try {
            val messagesDTO = AIAnswerAssistantMapper.messagesToDtos(messagesFromUIKit)
            val answer = aiDataSource.createAnswerWithProxyServer(messagesDTO, token)

            return answer
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