/*
 * Created by Injoit on 6.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.event

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
import com.quickblox.android_ui_kit.data.repository.mapper.DialogMapper
import com.quickblox.android_ui_kit.data.repository.mapper.MessageMapper
import com.quickblox.android_ui_kit.data.repository.mapper.TypingMapper
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.TypingEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventsRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : EventsRepository {
    private val exceptionFactory: EventsRepositoryExceptionFactory = EventsRepositoryExceptionFactoryImpl()

    override fun startTypingEvent(dialogEntity: DialogEntity) {
        val dialogDTO = DialogMapper.remoteDTOFrom(dialogEntity)
        remoteDataSource.startTyping(dialogDTO)
    }

    override fun stopTypingEvent(dialogEntity: DialogEntity) {
        val dialogDTO = DialogMapper.remoteDTOFrom(dialogEntity)
        remoteDataSource.stopTyping(dialogDTO)
    }

    override fun subscribeTypingEvents(): Flow<Pair<Int?, TypingEntity.TypingTypes?>> {
        return remoteDataSource.subscribeTypingEvent().map { remoteTypingDTO ->
            val type = TypingMapper.parseTypeFrom(remoteTypingDTO?.type)
            val senderId = TypingMapper.getSenderIdFrom(remoteTypingDTO?.senderId)
            Pair(senderId, type)
        }
    }

    // TODO: Need to rename to subscribeLocalDialogEvents
    override fun subscribeDialogEvents(): Flow<DialogEntity?> {
        return localDataSource.subscribeLocalUpdateDialogs().map { localDialogDTO ->
            localDialogDTO?.let {
                DialogMapper.toEntity(localDialogDTO)
            }
        }
    }

    override fun subscribeMessageEvents(): Flow<MessageEntity?> {
        return remoteDataSource.subscribeMessagesEvent().map { remoteMessageDTO ->
            val isRemoteMessageExist = remoteMessageDTO != null
            val isNotExistType = remoteMessageDTO?.type == null
            if (isRemoteMessageExist && isNotExistType) {
                throw exceptionFactory.makeIncorrectData("The remoteMessageDTO contains null value for \"type\" field")
            }

            val mappedMessageEntity = parseMessageEntityFrom(remoteMessageDTO)
            mappedMessageEntity
        }
    }

    private fun parseMessageEntityFrom(dto: RemoteMessageDTO?): MessageEntity? {
        if (dto == null) {
            return null
        }

        val isEventMessage = dto.type != CHAT_MESSAGE
        if (isEventMessage) {
            return MessageMapper.eventEntityFrom(dto)
        } else {
            return getChatEntityFrom(dto)
        }
    }

    private fun getChatEntityFrom(dto: RemoteMessageDTO): ChatMessageEntity {
        if (dto.outgoing == true) {
            return MessageMapper.outgoingChatEntityFrom(dto)
        } else {
            return MessageMapper.incomingChatEntityFrom(dto)
        }
    }
}