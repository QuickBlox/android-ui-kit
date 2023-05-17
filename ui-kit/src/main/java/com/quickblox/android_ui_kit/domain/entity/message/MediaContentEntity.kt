/*
 * Created by Injoit on 5.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

interface MediaContentEntity {
    enum class Types(val value: String) {
        IMAGE("image"),
        AUDIO("audio"),
        VIDEO("video"),
        FILE("application")
    }

    fun getName(): String

    fun getType(): Types

    fun getUrl(): String

    fun getMimeType(): String

    fun isGif(): Boolean
}