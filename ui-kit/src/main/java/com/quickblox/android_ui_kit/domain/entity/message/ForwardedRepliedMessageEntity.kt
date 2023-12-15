/*
 * Created by Injoit on 13.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

interface ForwardedRepliedMessageEntity : ChatMessageEntity {
    enum class Types { FORWARDED, REPLIED }

    fun isForwardedOrReplied(): Boolean

    fun setForwardOrReplied(type: Types)
    fun getForwardOrRepliedType(): Types?

    fun isReplied(): Boolean

    fun isForwarded(): Boolean

    fun getForwardedRepliedMessages(): List<ForwardedRepliedMessageEntity>?

    fun setForwardedRepliedMessages(messages: List<ForwardedRepliedMessageEntity>?)

    fun setRelatedMessageId(messageId: String?)

    fun getRelatedMessageId(): String?
}