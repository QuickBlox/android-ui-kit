/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

import com.quickblox.android_ui_kit.domain.entity.UserEntity

interface IncomingChatMessageEntity : ChatMessageEntity {
    fun getSender(): UserEntity?
    fun setSender(userEntity: UserEntity?)
}