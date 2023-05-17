/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

interface EventMessageEntity : MessageEntity {
    enum class EventTypes {
        CREATED_DIALOG,
        ADDED_USER_TO_DIALOG,
        LEFT_USER_FROM_DIALOG,
        REMOVED_USER_FROM_DIALOG,
        STARTED_TYPING,
        STOPPED_TYPING,
        READ_MESSAGE,
        DELIVERED_MESSAGE
    }

    fun setEventType(type: EventTypes?)
    fun getEventType(): EventTypes?

    fun setText(text: String?)
    fun getText(): String?
}