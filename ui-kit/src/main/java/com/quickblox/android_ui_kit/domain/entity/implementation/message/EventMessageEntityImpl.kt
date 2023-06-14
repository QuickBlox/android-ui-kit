/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import kotlin.random.Random

open class EventMessageEntityImpl : EventMessageEntity {
    private var messageId: String? = null
    private var dialogId: String? = null
    private var text: String? = null
    private var time: Long? = null
    private var participantId: Int? = null
    private var eventType: EventMessageEntity.EventTypes? = null
    private var senderId: Int? = null
    private var loggedUserId: Int? = null
    private var readIds: Collection<Int>? = null
    private var deliveredIds: Collection<Int>? = null

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

    override fun getSenderId(): Int? {
        return senderId
    }

    override fun setSenderId(id: Int?) {
        senderId = id
    }

    override fun setLoggedUserId(id: Int?) {
        loggedUserId = id
    }

    override fun setReadIds(ids: Collection<Int>?) {
        readIds = ids
    }

    override fun setDeliveredIds(ids: Collection<Int>?) {
        deliveredIds = ids
    }

    override fun isNotDelivered(): Boolean {
        val isContains = deliveredIds?.contains(loggedUserId)
        val isNotContains = !(isContains ?: false)
        return isNotContains
    }

    override fun isNotRead(): Boolean {
        val isContains = readIds?.contains(loggedUserId)
        val isNotContains = !(isContains ?: false)
        return isNotContains
    }

    override fun setParticipantId(participantId: Int?) {
        this.participantId = participantId
    }

    override fun getParticipantId(): Int? {
        return participantId
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MessageEntity) {
            messageId != null && messageId == other.geMessageId()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        val messageIdHashCode = if (messageId != null) {
            messageId.hashCode()
        } else {
            Random.nextInt(1000, 100000)
        }

        var hash = 1
        hash = 31 * hash + messageIdHashCode
        return hash
    }
}