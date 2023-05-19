/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity.message

interface MessageEntity {
    enum class MessageTypes { CHAT, EVENT }

    fun getMessageType(): MessageTypes

    fun geMessageId(): String?
    fun setMessageId(messageId: String?)

    fun getDialogId(): String?
    fun setDialogId(dialogId: String?)

    fun setTime(time: Long?)
    fun getTime(): Long?

    fun setParticipantId(participantId: Int?)
    fun getParticipantId(): Int?

    override fun equals(other: Any?): Boolean
}