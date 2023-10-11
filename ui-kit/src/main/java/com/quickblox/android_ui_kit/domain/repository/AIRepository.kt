/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity

interface AIRepository {
    fun translateIncomingMessageByOpenAIToken(messageEntity: IncomingChatMessageEntity): AITranslateIncomingChatMessageEntity
    fun translateIncomingMessageByQuickBloxToken(
        messageEntity: IncomingChatMessageEntity, token: String,
    ): AITranslateIncomingChatMessageEntity

    fun rephraseWithApiKE(
        toneEntity: AIRephraseEntity,
        messagesFromUIKit: List<MessageEntity>,
    ): AIRephraseEntity

    fun rephraseWithProxyServer(
        toneEntity: AIRephraseEntity,
        token: String,
        messagesFromUIKit: List<MessageEntity>,
    ): AIRephraseEntity

    fun getAllRephraseTones(): List<AIRephraseToneEntity>
    fun setAllRephraseTones(rephraseTones: List<AIRephraseToneEntity>)
}