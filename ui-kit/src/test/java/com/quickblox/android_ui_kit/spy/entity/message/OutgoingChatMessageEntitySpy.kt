/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity.message

import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.stub.entity.message.OutgoingChatMessageEntityStub
import java.util.*
import kotlin.random.Random

open class OutgoingChatMessageEntitySpy : OutgoingChatMessageEntityStub() {
    private var messageId: String? = UUID.randomUUID().toString()
    private var dialogId: String? = UUID.randomUUID().toString()
    private var messageType: MessageEntity.MessageTypes = MessageEntity.MessageTypes.CHAT
    private var content: String? = "content: ${UUID.randomUUID()}"
    private var time: Long? = Random.nextLong(1000, 10000)
    private var outgoingStates: OutgoingChatMessageEntity.OutgoingStates? = null
    private var participantId: Int? = null
    private var senderId: Int? = null
    private var mediaContent: MediaContentEntity? =
        MediaContentEntityImpl("temp_song", "https://test.com/temp_song.mp3", "audio/mpeg")

    override fun geMessageId(): String? {
        return messageId
    }

    override fun getDialogId(): String? {
        return dialogId
    }

    override fun getMessageType(): MessageEntity.MessageTypes {
        return messageType
    }

    override fun setMessageId(messageId: String?) {
        this.messageId = messageId
    }

    override fun setDialogId(dialogId: String?) {
        this.dialogId = dialogId
    }

    override fun setOutgoingState(state: OutgoingChatMessageEntity.OutgoingStates?) {
        outgoingStates = state
    }

    override fun getOutgoingState(): OutgoingChatMessageEntity.OutgoingStates? {
        return outgoingStates
    }

    override fun getContent(): String? {
        return content
    }

    override fun setContent(content: String?) {
        this.content = content
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

    override fun getSenderId(): Int? {
        return senderId
    }

    override fun setSenderId(id: Int?) {
        senderId = id
    }

    override fun getChatMessageType(): ChatMessageEntity.ChatMessageTypes {
        return ChatMessageEntity.ChatMessageTypes.FROM_LOGGED_USER
    }

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        return ChatMessageEntity.ContentTypes.TEXT
    }

    override fun getMediaContent(): MediaContentEntity? {
        return mediaContent
    }

    override fun setMediaContent(mediaContent: MediaContentEntity?) {
        this.mediaContent = mediaContent
    }
}