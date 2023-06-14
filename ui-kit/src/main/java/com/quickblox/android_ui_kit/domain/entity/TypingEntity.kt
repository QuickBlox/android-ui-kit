/*
 * Created by Injoit on 26.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

interface TypingEntity {
    enum class TypingTypes { STARTED, STOPPED }

    fun isStarted(): Boolean

    fun isStopped(): Boolean

    fun getText(): String

    fun setText(typingNames: String)
}