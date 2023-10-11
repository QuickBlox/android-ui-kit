/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.ai

import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseMessageDTO
import com.quickblox.android_ui_kit.data.dto.ai.AIRephraseToneDTO
import com.quickblox.android_ui_kit.data.dto.ai.AITranslateDTO

interface AIDataSource {
    fun translateIncomingMessageByOpenAIToken(translateDTO: AITranslateDTO): AITranslateDTO
    fun translateIncomingMessageByQuickBloxToken(
        translateDTO: AITranslateDTO,
        token: String,
    ): AITranslateDTO

    fun rephraseWithApiKey(rephraseDTO: AIRephraseDTO, messagesDTO: List<AIRephraseMessageDTO>): AIRephraseDTO
    fun rephraseWithProxyServer(
        rephraseDTO: AIRephraseDTO,
        token: String,
        messagesDTO: List<AIRephraseMessageDTO>,
    ): AIRephraseDTO

    fun getAllRephraseTones(): List<AIRephraseToneDTO>
    fun setAllRephraseTones(rephraseTones: List<AIRephraseToneDTO>)
}