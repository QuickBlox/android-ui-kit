/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity

open class EventMessageEntityImpl : EventMessageEntity {
    private var messageId: String? = null
    private var dialogId: String? = null
    private var text: String? = null
    private var time: Long? = null
    private var participantId: Int? = null
    private var eventType: EventMessageEntity.EventTypes? = null

    override fun setEventType(type: EventMessageEntity.EventTypes?) {
        this.eventType = type
    }

    override fun getEventType(): EventMessageEntity.EventTypes? {
        return eventType
    }

    override fun setText(text: String?) {
        this.text = text
    }

    override fun getText(): String? {
        return text
    }

    override fun getMessageType(): MessageEntity.MessageTypes {
        return MessageEntity.MessageTypes.EVENT
    }

    override fun geMessageId(): String? {
        return messageId
    }

    override fun setMessageId(messageId: String?) {
        this.messageId = messageId
    }

    override fun getDialogId(): String? {
        return dialogId
    }

    override fun setDialogId(dialogId: String?) {
        this.dialogId = dialogId
    }

    override fun setTime(time: Long?) {
        this.time = time
    }

    override fun getTime(): Long? {
        return time
    }

    override fun setParticipantId(participantId: Int?) {
        this.participantId = participantId
    }

    override fun getParticipantId(): Int? {
        return participantId
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MessageEntity) {
            messageId == other.geMessageId()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + messageId.hashCode()
        return hash
    }
}