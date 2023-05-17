/*
 * Created by Injoit on 17.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.domain.entity.CustomDataEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity

class DialogEntityImpl : DialogEntity {
    private var id: String? = null
    private var name: String? = null
    private var dialogType: DialogEntity.Types? = null
    private var ownerId: Int? = null
    private var updatedAt: String? = null
    private var lastMessage: IncomingChatMessageEntity? = null
    private var unreadMessagesCount: Int? = null
    private var customData: CustomDataEntity? = null
    private var participantIds: Collection<Int>? = null
    private var photo: String? = null
    private var isOwner: Boolean? = null

    override fun getDialogId(): String? {
        return id
    }

    override fun setDialogId(id: String?) {
        this.id = id
    }

    override fun getName(): String? {
        return name
    }

    override fun setName(name: String?) {
        this.name = name
    }

    override fun getType(): DialogEntity.Types? {
        return dialogType
    }

    override fun setDialogType(dialogType: DialogEntity.Types?) {
        this.dialogType = dialogType
    }

    override fun getOwnerId(): Int? {
        return ownerId
    }

    override fun setOwnerId(ownerId: Int?) {
        this.ownerId = ownerId
    }

    override fun getUpdatedAt(): String? {
        return updatedAt
    }

    override fun setUpdatedAt(updatedAt: String?) {
        this.updatedAt = updatedAt
    }

    override fun getLastMessage(): IncomingChatMessageEntity? {
        return lastMessage
    }

    override fun setLastMessage(lastMessage: IncomingChatMessageEntity?) {
        this.lastMessage = lastMessage
    }

    override fun getUnreadMessagesCount(): Int? {
        return unreadMessagesCount
    }

    override fun setUnreadMessagesCount(unreadMessagesCount: Int?) {
        this.unreadMessagesCount = unreadMessagesCount
    }

    override fun getCustomData(): CustomDataEntity? {
        return customData
    }

    override fun setCustomData(customData: CustomDataEntity?) {
        this.customData = customData
    }

    override fun getParticipantIds(): Collection<Int>? {
        return participantIds
    }

    override fun setParticipantIds(participantIds: Collection<Int>?) {
        this.participantIds = participantIds
    }

    override fun getPhoto(): String? {
        return photo
    }

    override fun setPhoto(photo: String?) {
        this.photo = photo
    }

    override fun isOwner(): Boolean? {
        return isOwner
    }

    override fun setIsOwner(isOwner: Boolean?) {
        this.isOwner = isOwner
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DialogEntityImpl) {
            id == other.getDialogId()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + id.hashCode()
        return hash
    }
}