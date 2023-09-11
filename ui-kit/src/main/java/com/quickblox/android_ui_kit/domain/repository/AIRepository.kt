/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity

interface AIRepository {
    fun translateIncomingMessageByOpenAIToken(messageEntity: IncomingChatMessageEntity): AITranslateIncomingChatMessageEntity
    fun translateIncomingMessageByQuickBloxToken(
        messageEntity: IncomingChatMessageEntity, token: String,
    ): AITranslateIncomingChatMessageEntity

    fun rephraseByOpenAIToken(toneEntity: AIRephraseToneEntity): AIRephraseToneEntity
    fun rephraseByQuickBloxToken(toneEntity: AIRephraseToneEntity, token: String): AIRephraseToneEntity
    fun getAllRephraseTones(): List<AIRephraseToneEntity>
}