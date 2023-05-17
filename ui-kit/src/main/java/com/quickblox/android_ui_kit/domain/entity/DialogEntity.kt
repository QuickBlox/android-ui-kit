/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import java.io.Serializable

interface DialogEntity : Serializable {
    enum class Types(val code: Int) { PUBLIC(1), GROUP(2), PRIVATE(3) }

    fun getDialogId(): String?
    fun setDialogId(id: String?)

    fun getName(): String?
    fun setName(name: String?)

    fun getType(): Types?
    fun setDialogType(dialogType: Types?)

    fun getOwnerId(): Int?
    fun setOwnerId(ownerId: Int?)

    fun getUpdatedAt(): String?
    fun setUpdatedAt(updatedAt: String?)

    fun getLastMessage(): IncomingChatMessageEntity?
    fun setLastMessage(lastMessage: IncomingChatMessageEntity?)

    fun getUnreadMessagesCount(): Int?
    fun setUnreadMessagesCount(unreadMessagesCount: Int?)

    fun getCustomData(): CustomDataEntity?
    fun setCustomData(customData: CustomDataEntity?)

    fun getParticipantIds(): Collection<Int>?
    fun setParticipantIds(participantIds: Collection<Int>?)

    fun getPhoto(): String?
    fun setPhoto(photo: String?)

    fun isOwner(): Boolean?
    fun setIsOwner(isOwner: Boolean?)

    override fun equals(other: Any?): Boolean
}