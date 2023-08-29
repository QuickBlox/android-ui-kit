/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.repository.ai

import com.quickblox.android_ui_kit.data.repository.mapper.AITranslateMapper
import com.quickblox.android_ui_kit.data.source.ai.AIDataSource
import com.quickblox.android_ui_kit.data.source.exception.AIDataSourceException
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
}