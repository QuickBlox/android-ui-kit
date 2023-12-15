/*
 * Created by Injoit on 11.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity

class AITranslateIncomingChatMessageEntity(contentType: ChatMessageEntity.ContentTypes) :
    IncomingChatMessageEntityImpl(contentType) {
    private var translation: String? = null
    private var isTranslated: Boolean = false

    fun getTranslation(): String? {
        return translation
    }

    fun setTranslation(translation: String?) {
        this.translation = translation
    }

    fun setTranslated(isTranslated: Boolean) {
        this.isTranslated = isTranslated
    }

    fun isTranslated(): Boolean {
        return isTranslated
    }

    override fun getContentType(): ChatMessageEntity.ContentTypes {
        return super.getContentType()
    }
}