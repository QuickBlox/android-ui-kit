/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity

import com.quickblox.android_ui_kit.domain.entity.CustomDataEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.spy.entity.message.IncomingChatMessageEntitySpy
import java.util.*
import kotlin.random.Random

open class DialogEntitySpy : DialogEntity {
    private var dialogId: String? = UUID.randomUUID().toString()
    private var name: String? = UUID.randomUUID().toString()
    private var type: DialogEntity.Types? = DialogEntity.Types.PRIVATE
    private var ownerId: Int? = Random.nextInt(1000, 10000)
    private var updatedId: String? = UUID.randomUUID().toString()
    private var updatedAt: String? = UUID.randomUUID().toString()
    private var lastMessage: IncomingChatMessageEntity? = IncomingChatMessageEntitySpy()
    private var unreadMessagesCount: Int? = Random.nextInt(1, 95)
    private var customData: CustomDataEntity? = CustomDataEntitySpy()
    private var participantIds: Collection<Int>? = arrayListOf(Random.nextInt(1, 95), Random.nextInt(1, 95))
    private var photo: String? = "https:// ${UUID.randomUUID()}"
    private var isOwner: Boolean? = false

    override fun getDialogId(): String? {
        return dialogId
    }

    override fun setDialogId(dialogId: String?) {
        this.dialogId = dialogId
    }

    override fun getName(): String? {
        return name
    }

    override fun setName(name: String?) {
        this.name = name
    }

    override fun getType(): DialogEntity.Types? {
        return type
    }

    override fun setDialogType(dialogType: DialogEntity.Types?) {
        this.type = dialogType
    }

    override fun getOwnerId(): Int? {
        return ownerId
    }

    override fun setOwnerId(ownerId: Int?) {
        this.ownerId = ownerId
    }

    override fun getUpdatedAt(): String? {
        return updatedId
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
            getDialogId() == other.getDialogId()
        } else {
            false
        }
    }
}