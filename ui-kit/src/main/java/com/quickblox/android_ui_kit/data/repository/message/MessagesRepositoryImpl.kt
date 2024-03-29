/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.message

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.repository.mapper.DialogMapper
import com.quickblox.android_ui_kit.data.repository.mapper.MessageMapper
import com.quickblox.android_ui_kit.data.repository.mapper.MessagePaginationMapper
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class MessagesRepositoryImpl(private val remoteDataSource: RemoteDataSource) : MessagesRepository {
    private val exceptionFactory: MessagesRepositoryExceptionFactory = MessagesRepositoryExceptionFactoryImpl()

    override fun getMessagesFromRemote(
        dialogId: String,
        paginationEntity: PaginationEntity,
    ): Flow<Result<Pair<MessageEntity?, PaginationEntity>>> {
        val paginationDTO = MessagePaginationMapper.dtoFrom(paginationEntity)

        val remoteMessageDTO = RemoteMessageDTO()
        remoteMessageDTO.dialogId = dialogId

        return channelFlow {
            try {
                remoteDataSource.getAllMessages(remoteMessageDTO, paginationDTO).onEach { result ->
                    if (result.isSuccess) {
                        val receivedMessageDTO = result.getOrThrow().first

                        val isRemoteMessageExist = receivedMessageDTO != null
                        val isNotExistType = receivedMessageDTO?.type == null
                        if (isRemoteMessageExist && isNotExistType) {
                            throw exceptionFactory.makeIncorrectData("The remoteMessageDTO contains null value for \"type\" field")
                        }

                        val mappedMessageEntity = parseMessageEntityFrom(receivedMessageDTO)

                        val receivedPaginationDTO = result.getOrThrow().second
                        val mappedPaginationEntity = MessagePaginationMapper.entityFrom(receivedPaginationDTO)

                        send(Result.success(Pair(mappedMessageEntity, mappedPaginationEntity)))
                    }
                    if (result.isFailure) {
                        val exception = result.getOrThrow() as Exception
                        send(Result.failure(exception))
                    }
                }.collect()
            } catch (exception: RemoteDataSourceException) {
                throw exceptionFactory.makeUnexpected(exception.message.toString())
            }
        }.buffer(1)
    }


    private fun parseMessageEntityFrom(dto: RemoteMessageDTO?): MessageEntity? {
        if (dto == null) {
            return null
        }

        val isEventMessage = dto.type != RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        if (isEventMessage) {
            return MessageMapper.eventEntityFrom(dto)
        } else {
            return getChatEntityFrom(dto)
        }
    }

    private fun getChatEntityFrom(dto: RemoteMessageDTO): MessageEntity {
        if (dto.outgoing == true) {
            return MessageMapper.outgoingChatEntityFrom(dto)
        } else {
            return MessageMapper.incomingChatEntityFrom(dto)
        }
    }

    override fun updateMessageInRemote(entity: MessageEntity) {
        try {
            val dto = MessageMapper.remoteDTOFromChatMessage(entity as ChatMessageEntity)
            remoteDataSource.updateMessage(dto)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deleteMessageInRemote(entity: MessageEntity) {
        try {
            val dto = MessageMapper.remoteDTOFromChatMessage(entity as ChatMessageEntity)
            remoteDataSource.deleteMessage(dto)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createMessage(entity: OutgoingChatMessageEntity): OutgoingChatMessageEntity {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromChatMessage(entity)
            val createdMessage = remoteDataSource.createMessage(remoteMessageDTO)

            // TODO: Need to move this logic to UseCase. The logic for changing status should be inside use case.
            createdMessage.outgoingState = RemoteMessageDTO.OutgoingMessageStates.SENDING

            return MessageMapper.outgoingChatEntityFrom(createdMessage)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createForwardMessage(
        forwardMessages: List<ForwardedRepliedMessageEntity>,
        relateMessage: OutgoingChatMessageEntity
    ): OutgoingChatMessageEntity {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromForwardChatMessage(forwardMessages, relateMessage)
            val createdMessageDTO = remoteDataSource.createMessage(remoteMessageDTO)

            // TODO: Need to move this logic to UseCase. The logic for changing status should be inside use case.
            createdMessageDTO.outgoingState = RemoteMessageDTO.OutgoingMessageStates.SENDING

            return MessageMapper.outgoingChatEntityFrom(createdMessageDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createReplyMessage(
        replyMessage: ForwardedRepliedMessageEntity,
        relateMessage: OutgoingChatMessageEntity
    ): OutgoingChatMessageEntity {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromReplyChatMessage(replyMessage, relateMessage)
            val createdMessageDTO = remoteDataSource.createMessage(remoteMessageDTO)

            // TODO: Need to move this logic to UseCase. The logic for changing status should be inside use case.
            createdMessageDTO.outgoingState = RemoteMessageDTO.OutgoingMessageStates.SENDING

            val outgoingMessage = MessageMapper.outgoingChatEntityFrom(createdMessageDTO)
            return outgoingMessage
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun readMessage(entity: MessageEntity, dialog: DialogEntity) {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromMessageEntity(entity)
            val remoteDialogDTO = DialogMapper.remoteDTOFrom(dialog)
            remoteDataSource.readMessage(remoteMessageDTO, remoteDialogDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deliverMessage(entity: MessageEntity, dialog: DialogEntity) {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromMessageEntity(entity)
            val remoteDialogDTO = DialogMapper.remoteDTOFrom(dialog)
            remoteDataSource.deliverMessage(remoteMessageDTO, remoteDialogDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity) {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromOutgoingChatMessage(entity)
            val remoteDialogDTO = DialogMapper.remoteDTOFrom(dialog)
            remoteDataSource.sendChatMessage(remoteMessageDTO, remoteDialogDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun sendEventMessageToRemote(entity: EventMessageEntity, dialog: DialogEntity) {
        try {
            val remoteMessageDTO = MessageMapper.remoteDTOFromEventMessage(entity)
            val remoteDialogDTO = DialogMapper.remoteDTOFrom(dialog)
            remoteDataSource.sendEventMessage(remoteMessageDTO, remoteDialogDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }
}