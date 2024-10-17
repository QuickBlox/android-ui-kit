/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity

interface AIRepository {
    fun translateIncomingMessageWithSmartChatAssistantId(
        messageEntity: ForwardedRepliedMessageEntity,
        messagesFromUIKit: List<MessageEntity>, languageCode: String,
    ): AITranslateIncomingChatMessageEntity

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun translateIncomingMessageWithApiKey(
        messageEntity: ForwardedRepliedMessageEntity,
        messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun translateIncomingMessageWithProxyServer(
        messageEntity: ForwardedRepliedMessageEntity, token: String, messagesFromUIKit: List<MessageEntity>,
    ): AITranslateIncomingChatMessageEntity

    fun createAnswerWithSmartChatAssistantId(messageToAssist: String, historyMessagesDTO: List<MessageEntity>): String

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun createAnswerWithApiKey(messagesFromUIKit: List<MessageEntity>): String

    @Deprecated("This method is deprecated and will be removed in future versions.")
    fun createAnswerWithProxyServer(messagesFromUIKit: List<MessageEntity>, token: String): String

    fun rephraseWithApiKey(
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