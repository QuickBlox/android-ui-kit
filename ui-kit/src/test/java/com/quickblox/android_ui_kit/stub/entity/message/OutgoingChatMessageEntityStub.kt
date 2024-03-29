/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity

open class OutgoingChatMessageEntityStub : MessageEntityStub(), OutgoingChatMessageEntity {
    override fun getOutgoingState(): OutgoingChatMessageEntity.OutgoingStates? {
        throw buildRuntimeException()
    }

    override fun setOutgoingState(state: OutgoingChatMessageEntity.OutgoingStates?) {
        throw buildRuntimeException()
    }

    override fun setTime(time: Long?) {
        throw buildRuntimeException()
    }

    override fun getTime(): Long? {
        throw buildRuntimeException()
    }

    override fun equals(other: Any?): Boolean {
        throw buildRuntimeException()
    }

    override fun getContent(): String? {
        throw buildRuntimeException()
    }

    override fun setContent(content: String?) {
        throw buildRuntimeException()
    }

    override fun getChatMessageType(): ChatMessageEntity.ChatMessageTypes {
        throw buildRuntimeException()
    }

    override fun isMediaContent(): Boolean {
        throw buildRuntimeException()
    }

    override fun getMediaContent(): MediaContentEntity? {
        throw buildRuntimeException()
    }

    override fun setMediaContent(content: MediaContentEntity?) {
        throw buildRuntimeException()
    }

    override fun isForwardedOrReplied(): Boolean {
        throw buildRuntimeException()
    }

    override fun setForwardOrReplied(type: ForwardedRepliedMessageEntity.Types) {
        throw buildRuntimeException()
    }

    override fun getForwardOrRepliedType(): ForwardedRepliedMessageEntity.Types? {
        throw buildRuntimeException()
    }

    override fun isReplied(): Boolean {
        throw buildRuntimeException()
    }

    override fun isForwarded(): Boolean {
        throw buildRuntimeException()
    }

    override fun getForwardedRepliedMessages(): List<ForwardedRepliedMessageEntity>? {
        throw buildRuntimeException()
    }

    override fun setForwardedRepliedMessages(messages: List<ForwardedRepliedMessageEntity>?) {
        throw buildRuntimeException()
    }

    override fun setRelatedMessageId(messageId: String?) {
        throw buildRuntimeException()
    }

    override fun getRelatedMessageId(): String? {
        throw buildRuntimeException()
    }

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        throw buildRuntimeException()
    }

    override fun getSender(): UserEntity? {
        throw buildRuntimeException()
    }

    override fun setSender(userEntity: UserEntity?) {
        throw buildRuntimeException()
    }
}