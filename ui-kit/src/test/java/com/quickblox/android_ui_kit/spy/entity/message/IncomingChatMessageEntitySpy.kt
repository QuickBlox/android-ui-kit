/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.stub.entity.message.IncomingChatMessageEntityStub
import java.util.UUID
import kotlin.random.Random

open class IncomingChatMessageEntitySpy : IncomingChatMessageEntityStub() {
    private var messageId: String? = UUID.randomUUID().toString()
    private var dialogId: String? = UUID.randomUUID().toString()
    private var messageType: MessageEntity.MessageTypes = MessageEntity.MessageTypes.CHAT
    private var senderId: Int? = Random.nextInt(0, 1000)
    private var sender: UserEntity? = null
    private var content: String? = System.currentTimeMillis().toString()
    private var time: Long? = System.currentTimeMillis()

    private var loggedUserId: Int? = null
    private var readIds: Collection<Int>? = null
    private var deliveredIds: Collection<Int>? = null

    override fun getMessageId(): String? {
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

    override fun isNotDelivered(): Boolean {
        return deliveredIds?.contains(loggedUserId) == true
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

    override fun isNotRead(): Boolean {
        return readIds?.contains(loggedUserId) == true
    }

    override fun getSenderId(): Int? {
        return senderId
    }

    override fun setSenderId(id: Int?) {
        senderId = id
    }

    override fun setSender(userEntity: UserEntity?) {
        sender = userEntity
    }

    override fun getSender(): UserEntity? {
        return sender
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

    override fun isForwardedOrReplied(): Boolean {
        return false
    }

    override fun getForwardedRepliedMessages(): List<ForwardedRepliedMessageEntity>? {
        return listOf()
    }

    override fun equals(other: Any?): Boolean {
        return true
    }
}