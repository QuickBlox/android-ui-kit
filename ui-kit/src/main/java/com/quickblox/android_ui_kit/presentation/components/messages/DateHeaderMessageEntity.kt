/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages

import com.quickblox.android_ui_kit.domain.entity.implementation.message.EventMessageEntityImpl

// TODO: Need to move this class in EventMessageEntity as a separate type.
//  Since the logic for adding a Date Header must be at the domain level.
//  Also need to create in EventMessageEntity specific types of events, for example, a user has been added, a user has been deleted
class DateHeaderMessageEntity(id: String? = null, private var messageText: String? = null) :
    EventMessageEntityImpl() {
    init {
        val randomMessageId = System.currentTimeMillis().toString()
        setMessageId(randomMessageId)
        setDialogId(id)
    }

    override fun getText(): String? {
        return messageText
    }
}