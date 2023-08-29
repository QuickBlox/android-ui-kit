/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai

import com.quickblox.android_ai_translate.QBAITranslate
import com.quickblox.android_ai_translate.exception.QBAITranslateException
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateDTO
import com.quickblox.android_ui_kit.data.source.exception.AIDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

class AIDataSourceImpl : AIDataSource {
    override fun translateIncomingMessageByOpenAIToken(translateDTO: AITranslateDTO): AITranslateDTO {
        val openAIToken = QuickBloxUiKit.getOpenAIToken()

        try {
            val text = translateDTO.content

            if (text.isNullOrEmpty()) {
                throw AIDataSourceException("Content should not be null or empty")
            }

            val translations = QBAITranslate.executeByOpenAITokenSync(openAIToken, text)
            translateDTO.translations = translations

            return translateDTO
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }

    override fun translateIncomingMessageByQuickBloxToken(
        translateDTO: AITranslateDTO,
        token: String,
    ): AITranslateDTO {
        val proxyServerURL = QuickBloxUiKit.getProxyServerURL()

        try {
            val text = translateDTO.content

            if (text.isNullOrEmpty()) {
                throw AIDataSourceException("Content should not be null or empty")
            }

            val translations = QBAITranslate.executeByQBTokenSync(token, proxyServerURL, text, true)
            translateDTO.translations = translations

            return translateDTO
        } catch (exception: QBAITranslateException) {
            throw AIDataSourceException(exception.message)
        } catch (exception: MappingException) {
            throw AIDataSourceException(exception.message)
        }
    }
}