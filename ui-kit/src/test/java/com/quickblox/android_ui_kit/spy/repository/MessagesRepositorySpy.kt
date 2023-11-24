/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.entity.message.IncomingChatMessageEntitySpy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

open class MessagesRepositorySpy(private val remoteMessagesCount: Int = 10) : MessagesRepository {
    private var messages = mutableListOf<IncomingChatMessageEntity>()

    init {
        for (index in 1..remoteMessagesCount) {
            messages.add(IncomingChatMessageEntitySpy())
        }
    }

    override fun createMessage(entity: OutgoingChatMessageEntity): OutgoingChatMessageEntity {
        val outgoingState = OutgoingChatMessageEntity.OutgoingStates.SENT
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        return OutgoingChatMessageEntityImpl(outgoingState, contentType)
    }

    override fun createForwardMessage(
        forwardMessages: List<ForwardedRepliedMessageEntity>, relateMessage: OutgoingChatMessageEntity
    ): OutgoingChatMessageEntity {
        val outgoingState = OutgoingChatMessageEntity.OutgoingStates.SENDING
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        return OutgoingChatMessageEntityImpl(outgoingState, contentType).apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.FORWARDED)
        }
    }

    override fun readMessage(entity: MessageEntity, dialog: DialogEntity) {

    }

    override fun deliverMessage(entity: MessageEntity, dialog: DialogEntity) {

    }

    override fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity) {

    }

    override fun sendEventMessageToRemote(entity: EventMessageEntity, dialog: DialogEntity) {

    }

    override fun getMessagesFromRemote(
        dialogId: String, paginationEntity: PaginationEntity
    ): Flow<Result<Pair<MessageEntity, PaginationEntity>>> {
        return channelFlow {
            messages.forEach { message ->
                val pair = Pair(message, PaginationEntityImpl())
                send(Result.success(pair))
            }
        }
    }

    override fun updateMessageInRemote(entity: MessageEntity) {

    }

    override fun deleteMessageInRemote(entity: MessageEntity) {

    }
}