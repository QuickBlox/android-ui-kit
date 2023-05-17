/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.local.dialog

open class LocalDialogDTO {
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

    override fun equals(other: Any?): Boolean {
        return if (other is LocalDialogDTO) {
            return id == other.id
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        val hash = 1

        id?.let {
            return 31 * hash + id.hashCode()
        } ?: run {
            throw RuntimeException("the hashcode for $id is null")
        }
    }
}