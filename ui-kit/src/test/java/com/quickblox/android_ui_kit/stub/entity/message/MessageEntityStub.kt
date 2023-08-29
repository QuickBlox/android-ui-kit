/*
 * Created by Injoit on 27.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity.message

import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.stub.BaseStub

open class MessageEntityStub : BaseStub(), MessageEntity {
    override fun getMessageType(): MessageEntity.MessageTypes {
        throw buildRuntimeException()
    }

    override fun getMessageId(): String? {
        throw buildRuntimeException()
    }

    override fun setMessageId(messageId: String?) {
        throw buildRuntimeException()
    }

    override fun getDialogId(): String? {
        throw buildRuntimeException()
    }

    override fun setDialogId(dialogId: String?) {
        throw buildRuntimeException()
    }

    override fun setTime(time: Long?) {
        throw buildRuntimeException()
    }

    override fun getTime(): Long? {
        throw buildRuntimeException()
    }

    override fun getSenderId(): Int? {
        throw buildRuntimeException()
    }

    override fun setSenderId(id: Int?) {
        throw buildRuntimeException()
    }

    override fun setParticipantId(participantId: Int?) {
        throw buildRuntimeException()
    }

    override fun getParticipantId(): Int? {
        throw buildRuntimeException()
    }

    override fun equals(other: Any?): Boolean {
        throw buildRuntimeException()
    }
}