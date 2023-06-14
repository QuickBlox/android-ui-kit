/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity

open class IncomingChatMessageEntityStub : MessageEntityStub(), IncomingChatMessageEntity {
    override fun getSender(): UserEntity? {
        throw buildRuntimeException()
    }

    override fun setSender(userEntity: UserEntity?) {
        throw buildRuntimeException()
    }

    override fun getSenderId(): Int? {
        throw buildRuntimeException()
    }

    override fun setSenderId(id: Int?) {
        throw buildRuntimeException()
    }

    override fun setLoggedUserId(id: Int?) {
        throw buildRuntimeException()
    }

    override fun setReadIds(ids: Collection<Int>?) {
        throw buildRuntimeException()
    }

    override fun setDeliveredIds(ids: Collection<Int>?) {
        throw buildRuntimeException()
    }

    override fun isNotRead(): Boolean {
        throw buildRuntimeException()
    }

    override fun isNotDelivered(): Boolean {
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

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        throw buildRuntimeException()
    }
}