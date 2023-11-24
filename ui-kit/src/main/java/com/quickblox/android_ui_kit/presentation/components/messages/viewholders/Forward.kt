/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity

interface Forward {
    fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>)
}