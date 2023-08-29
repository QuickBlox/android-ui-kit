/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity

class AITranslateIncomingChatMessageEntity(private var contentType: ChatMessageEntity.ContentTypes) :
    IncomingChatMessageEntityImpl(contentType) {
    private var translations: List<String>? = null
    private var isTranslated: Boolean? = false

    fun getTranslations(): List<String>? {
        return translations
    }

    fun setTranslations(translations: List<String>?) {
        this.translations = translations
    }

    fun setTranslated(isTranslated: Boolean) {
        this.isTranslated = isTranslated
    }

    fun isTranslated(): Boolean? {
        return isTranslated
    }
}