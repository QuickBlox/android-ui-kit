/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.chat.utils.MongoDBObjectId

class OutgoingChatMessageEntityImpl(
    private var outgoingState: OutgoingChatMessageEntity.OutgoingStates?,
    private var contentType: ChatMessageEntity.ContentTypes
) : OutgoingChatMessageEntity {
    private var dialogId: String? = null
    private var messageId: String? = null
    private var time: Long? = null
    private var content: String? = null
    private var participantId: Int? = null
    private var mediaContent: MediaContentEntity? = null

    init {
        // TODO: need to move to data layer
        messageId = MongoDBObjectId.get().toString()
    }

    override fun getChatMessageType(): ChatMessageEntity.ChatMessageTypes {
        return ChatMessageEntity.ChatMessageTypes.FROM_LOGGED_USER
    }

    override fun isMediaContent(): Boolean {
        return contentType == ChatMessageEntity.ContentTypes.MEDIA
    }

    override fun getMediaContent(): MediaContentEntity? {
        return mediaContent
    }

    override fun setMediaContent(mediaContent: MediaContentEntity?) {
        this.mediaContent = mediaContent
    }

    override fun getSenderId(): Int? {
        TODO("Not yet implemented")
    }

    override fun setSenderId(id: Int?) {
        TODO("Not yet implemented")
    }

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        return contentType
    }

    override fun setOutgoingState(state: OutgoingChatMessageEntity.OutgoingStates?) {
        outgoingState = state
    }

    override fun getOutgoingState(): OutgoingChatMessageEntity.OutgoingStates? {
        return outgoingState
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

    override fun getContent(): String? {
        return content
    }

    override fun setContent(content: String?) {
        this.content = content
    }

    override fun getMessageType(): MessageEntity.MessageTypes {
        return MessageEntity.MessageTypes.CHAT
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