/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

interface IncomingChatMessageEntity : ForwardedRepliedMessageEntity {
    fun getLoggedUserId(): Int?
    fun setLoggedUserId(id: Int?)

    fun getReadIds(): Collection<Int>?
    fun setReadIds(ids: Collection<Int>?)

    fun getDeliveredIds(): Collection<Int>?
    fun setDeliveredIds(ids: Collection<Int>?)

    fun isNotRead(): Boolean
    fun isNotDelivered(): Boolean
}