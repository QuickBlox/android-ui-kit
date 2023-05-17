/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity

import com.quickblox.android_ui_kit.domain.entity.CustomDataEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity

open class DialogEntityStub : DialogEntity {
    override fun getDialogId(): String? {
        return null
    }

    override fun setDialogId(dialogId: String?) {

    }

    override fun getName(): String? {
        return null
    }

    override fun setName(name: String?) {

    }

    override fun getType(): DialogEntity.Types? {
        return null
    }

    override fun setDialogType(dialogType: DialogEntity.Types?) {

    }

    override fun getOwnerId(): Int? {
        return null
    }

    override fun setOwnerId(ownerId: Int?) {

    }

    override fun getUpdatedAt(): String? {
        return null
    }

    override fun setUpdatedAt(updatedAt: String?) {

    }

    override fun getLastMessage(): IncomingChatMessageEntity? {
        return null
    }

    override fun setLastMessage(lastMessage: IncomingChatMessageEntity?) {

    }

    override fun getUnreadMessagesCount(): Int? {
        return null
    }

    override fun setUnreadMessagesCount(unreadMessagesCount: Int?) {

    }

    override fun getCustomData(): CustomDataEntity? {
        return null
    }

    override fun setCustomData(customData: CustomDataEntity?) {

    }

    override fun getParticipantIds(): List<Int>? {
        return null
    }

    override fun setParticipantIds(participantIds: Collection<Int>?) {

    }

    override fun getPhoto(): String? {
        return null
    }

    override fun setPhoto(photo: String?) {

    }

    override fun isOwner(): Boolean? {
        return null
    }

    override fun setIsOwner(isOwner: Boolean?) {

    }

    override fun equals(other: Any?): Boolean {
        return false
    }
}