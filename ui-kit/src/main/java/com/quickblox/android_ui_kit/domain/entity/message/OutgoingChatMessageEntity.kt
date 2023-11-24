/*
 * Created by Injoit on 25.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.message

interface OutgoingChatMessageEntity : ForwardedRepliedMessageEntity {
    enum class OutgoingStates { SENDING, SENT, DELIVERED, READ, ERROR }

    fun getOutgoingState(): OutgoingStates?
    fun setOutgoingState(state: OutgoingStates?)
}