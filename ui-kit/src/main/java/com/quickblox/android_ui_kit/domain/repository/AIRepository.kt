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
    fun translateIncomingMessageWithApiKey(
        messageEntity: IncomingChatMessageEntity,
        messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity

    fun translateIncomingMessageWithProxyServer(
        messageEntity: IncomingChatMessageEntity, token: String, messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity

    fun createAnswerWithApiKey(messagesFromUIKit: List<MessageEntity>): String
    fun createAnswerWithProxyServer(messagesFromUIKit: List<MessageEntity>, token: String): String

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