/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.remote.message

class RemoteMessageDTO {
    enum class MessageTypes {
        CHAT_MESSAGE,
        EVENT_CREATED_DIALOG,
        EVENT_ADDED_USER,
        EVENT_REMOVED_USER,
        EVENT_LEFT_USER
    }

    enum class OutgoingMessageStates {
        SENDING,
        SENT,
        DELIVERED,
        READ,
    }

    var id: String? = null
    var type: MessageTypes? = null
    var dialogId: String? = null
    var text: String? = null
    var outgoing: Boolean? = null
    var senderId: Int? = null
    var time: Long? = null
    var participantId: Int? = null
    var needSendChatMessage: Boolean? = null
    var fileUrl: String? = null
    var fileName: String? = null
    var mimeType: String? = null
    var outgoingState: OutgoingMessageStates? = null
    var loggedUserId: Int? = null
    var readIds: Collection<Int>? = null
    var deliveredIds: Collection<Int>? = null
}