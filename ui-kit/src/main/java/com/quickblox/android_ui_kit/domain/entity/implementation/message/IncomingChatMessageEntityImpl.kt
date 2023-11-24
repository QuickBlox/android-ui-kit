/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.*
import kotlin.random.Random

open class IncomingChatMessageEntityImpl(private var contentType: ChatMessageEntity.ContentTypes) :
    IncomingChatMessageEntity {
    private var dialogId: String? = null
    private var senderId: Int? = null
    private var loggedUserId: Int? = null
    private var sender: UserEntity? = null
    private var messageId: String? = null
    private var time: Long? = null
    private var content: String? = null
    private var participantId: Int? = null
    private var mediaContent: MediaContentEntity? = null
    private var readIds: Collection<Int>? = null
    private var deliveredIds: Collection<Int>? = null

    private var forwardReplyType: ForwardedRepliedMessageEntity.Types? = null
    private var forwardedRepliedMessages: List<ForwardedRepliedMessageEntity>? = null

    private var relatedMessageId: String? = null

    override fun getChatMessageType(): ChatMessageEntity.ChatMessageTypes {
        return ChatMessageEntity.ChatMessageTypes.FROM_OPPONENT
    }

    override fun getMediaContent(): MediaContentEntity? {
        return mediaContent
    }

    override fun setMediaContent(mediaContent: MediaContentEntity?) {
        this.mediaContent = mediaContent
    }

    override fun isMediaContent(): Boolean {
        return contentType == ChatMessageEntity.ContentTypes.MEDIA
    }

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        return contentType
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

    override fun getSender(): UserEntity? {
        return sender
    }

    override fun setSender(userEntity: UserEntity?) {
        sender = userEntity
    }

    override fun getLoggedUserId(): Int? {
        return loggedUserId
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

    override fun getReadIds(): Collection<Int>? {
        return readIds
    }

    override fun getContent(): String? {
        return content
    }

    override fun setContent(content: String?) {
        this.content = content
    }

    override fun getMessageType(): MessageEntity.MessageTypes {
        return MessageEntity.MessageTypes.CHAT
    }

    override fun getMessageId(): String? {
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

    override fun setReadIds(ids: Collection<Int>?) {
        readIds = ids
    }

    override fun getDeliveredIds(): Collection<Int>? {
        return deliveredIds
    }

    override fun setDeliveredIds(ids: Collection<Int>?) {
        deliveredIds = ids
    }

    override fun isNotRead(): Boolean {
        val isContains = readIds?.contains(loggedUserId)
        val isNotContains = !(isContains ?: false)
        return isNotContains
    }

    override fun isNotDelivered(): Boolean {
        val isContains = deliveredIds?.contains(loggedUserId)
        val isNotContains = !(isContains ?: false)
        return isNotContains
    }

    override fun setForwardOrReplied(type: ForwardedRepliedMessageEntity.Types) {
        forwardReplyType = type
    }

    override fun getForwardOrRepliedType(): ForwardedRepliedMessageEntity.Types? {
        return forwardReplyType
    }

    override fun isForwardedOrReplied(): Boolean {
        return forwardReplyType != null
    }

    override fun isReplied(): Boolean {
        return forwardReplyType == ForwardedRepliedMessageEntity.Types.REPLIED
    }

    override fun isForwarded(): Boolean {
        return forwardReplyType == ForwardedRepliedMessageEntity.Types.FORWARDED
    }

    override fun getForwardedRepliedMessages(): List<ForwardedRepliedMessageEntity>? {
        return forwardedRepliedMessages
    }

    override fun setForwardedRepliedMessages(messages: List<ForwardedRepliedMessageEntity>?) {
        forwardedRepliedMessages = messages
    }

    override fun setRelatedMessageId(messageId: String?) {
        this.relatedMessageId = messageId
    }

    override fun getRelatedMessageId(): String? {
        return relatedMessageId
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MessageEntity) {
            messageId != null && messageId == other.getMessageId()
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