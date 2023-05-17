/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.remote.dialog

class RemoteDialogDTO {
    var id: String? = null
    var type: Int? = null
    var ownerId: Int? = null
    var participantIds: Collection<Int>? = null
    var updatedAt: String? = null
    var lastMessageText: String? = null
    var lastMessageDateSent: Long? = null
    var lastMessageUserId: Int? = null
    var unreadMessageCount: Int? = null
    var name: String? = null
    var photo: String? = null
    var isOwner: Boolean? = null
}