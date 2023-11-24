/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity

interface ChatMessageEntity : MessageEntity {
    enum class ChatMessageTypes { FROM_OPPONENT, FROM_LOGGED_USER }
    enum class ContentTypes { TEXT, URL, LOCATION, MEDIA }

    fun getContentType(): ContentTypes

    fun getSender(): UserEntity?
    fun setSender(userEntity: UserEntity?)

    fun getContent(): String?
    fun setContent(content: String?)

    fun getChatMessageType(): ChatMessageTypes

    fun isMediaContent(): Boolean

    fun getMediaContent(): MediaContentEntity?

    fun setMediaContent(mediaContent: MediaContentEntity?)
}