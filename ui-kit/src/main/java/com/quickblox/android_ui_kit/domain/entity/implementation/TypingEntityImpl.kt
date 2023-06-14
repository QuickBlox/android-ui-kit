/*
 * Created by Injoit on 26.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.domain.entity.TypingEntity

class TypingEntityImpl() : TypingEntity {
    private var text: String = ""

    override fun isStarted(): Boolean {
        return text.isNotEmpty()
    }

    override fun isStopped(): Boolean {
        return text.isEmpty()
    }

    override fun setText(text: String) {
        this.text = text
    }

    override fun getText(): String {
        return text
    }
}