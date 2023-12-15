/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai

import com.quickblox.android_ui_kit.data.dto.ai.*

interface AIDataSource {
    fun translateIncomingMessageWithApiKey(translateDTO: AITranslateDTO, messagesDTO: List<AITranslateMessageDTO>): AITranslateDTO
    fun translateIncomingMessageWithProxyServer(
        translateDTO: AITranslateDTO,
        token: String,
        messagesDTO: List<AITranslateMessageDTO>
    ): AITranslateDTO

    fun createAnswerWithApiKey(messagesDTO: List<AIAnswerAssistantMessageDTO>): String
    fun createAnswerWithProxyServer(messagesDTO: List<AIAnswerAssistantMessageDTO>, token: String): String

    fun rephraseWithApiKey(rephraseDTO: AIRephraseDTO, messagesDTO: List<AIRephraseMessageDTO>): AIRephraseDTO
    fun rephraseWithProxyServer(
        rephraseDTO: AIRephraseDTO,
        token: String,
        messagesDTO: List<AIRephraseMessageDTO>,
    ): AIRephraseDTO

    fun getAllRephraseTones(): List<AIRephraseToneDTO>
    fun setAllRephraseTones(rephraseTones: List<AIRephraseToneDTO>)
}