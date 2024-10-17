/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai

import com.quickblox.android_ui_kit.data.dto.ai.AIAnswerAssistantMessageDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseMessageDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseToneDTO
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateDTO
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateMessageDTO

interface AIDataSource {
    fun translateIncomingMessageWithSmartChatAssistantId(
        translateDTO: AITranslateDTO,
        messagesDTO: List<AITranslateMessageDTO>,
        languageCode: String,
    ): AITranslateDTO

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun translateIncomingMessageWithApiKey(
        translateDTO: AITranslateDTO,
        messagesDTO: List<AITranslateMessageDTO>,
    ): AITranslateDTO

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun translateIncomingMessageWithProxyServer(
        translateDTO: AITranslateDTO,
        token: String,
        messagesDTO: List<AITranslateMessageDTO>,
    ): AITranslateDTO

    fun createAnswerWithSmartChatAssistantId(
        messageToAssist: String,
        historyMessagesDTO: List<AIAnswerAssistantMessageDTO>,
    ): String

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun createAnswerWithApiKey(messagesDTO: List<AIAnswerAssistantMessageDTO>): String

    @Deprecated("This method is deprecated and will be removed in future versions.")
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