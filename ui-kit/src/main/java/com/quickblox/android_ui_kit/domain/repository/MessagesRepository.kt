/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {
    @Throws(MessagesRepositoryException::class)
    fun createMessage(entity: OutgoingChatMessageEntity): OutgoingChatMessageEntity

    @Throws(MessagesRepositoryException::class)
    fun readMessage(entity: IncomingChatMessageEntity, dialog: DialogEntity)

    @Throws(MessagesRepositoryException::class)
    fun deliverMessage(entity: IncomingChatMessageEntity, dialog: DialogEntity)

    @Throws(MessagesRepositoryException::class)
    fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity)

    @Throws(MessagesRepositoryException::class)
    fun sendEventMessageToRemote(entity: EventMessageEntity, dialog: DialogEntity)

    @Throws(MessagesRepositoryException::class)
    fun getMessagesFromRemote(
        dialogId: String,
        paginationEntity: PaginationEntity
    ): Flow<Result<Pair<MessageEntity?, PaginationEntity>>>

    @Throws(MessagesRepositoryException::class)
    fun updateMessageInRemote(entity: MessageEntity)

    @Throws(MessagesRepositoryException::class)
    fun deleteMessageInRemote(entity: MessageEntity)
}