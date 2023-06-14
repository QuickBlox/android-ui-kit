/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogsDTO
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

object DialogMapper {
    fun dtoLocalFrom(remoteDialogDTO: RemoteDialogDTO): LocalDialogDTO {
        val dto = LocalDialogDTO()

        dto.type = remoteDialogDTO.type
        dto.name = remoteDialogDTO.name
        dto.id = remoteDialogDTO.id
        dto.ownerId = remoteDialogDTO.ownerId
        dto.updatedAt = remoteDialogDTO.updatedAt
        dto.lastMessageText = remoteDialogDTO.lastMessageText
        dto.lastMessageDateSent = remoteDialogDTO.lastMessageDateSent
        dto.lastMessageUserId = remoteDialogDTO.lastMessageUserId
        dto.unreadMessageCount = remoteDialogDTO.unreadMessageCount
        dto.photo = remoteDialogDTO.photo
        dto.isOwner = remoteDialogDTO.isOwner
        dto.participantIds = remoteDialogDTO.participantIds

        return dto
    }

    fun dtoLocalFrom(entity: DialogEntity): LocalDialogDTO {
        val dto = LocalDialogDTO()

        val type = validateTypeDialog(entity.getType()?.code)
        validateParticipantIdsInPrivateDialog(entity.getParticipantIds(), type)
        validateNameInGroupAndPublicDialog(entity.getName(), type)

        val isParticipantIdsNotNullOrEmpty = !entity.getParticipantIds().isNullOrEmpty()
        if (isParticipantIdsNotNullOrEmpty) {
            dto.participantIds = entity.getParticipantIds()
        }
        dto.type = convertIntFrom(entity.getType())
        dto.name = entity.getName()
        dto.id = entity.getDialogId()
        dto.ownerId = entity.getOwnerId()
        dto.updatedAt = entity.getUpdatedAt()
        dto.lastMessageText = entity.getLastMessage()?.getContent()
        dto.lastMessageDateSent = entity.getLastMessage()?.getTime() ?: 0
        dto.lastMessageUserId = entity.getLastMessage()?.getSenderId()
        dto.unreadMessageCount = entity.getUnreadMessagesCount()
        dto.photo = entity.getPhoto()
        dto.isOwner = entity.isOwner()
        return dto
    }

    fun remoteDTOFrom(entity: DialogEntity): RemoteDialogDTO {
        val dto = RemoteDialogDTO()

        val type = validateTypeDialog(entity.getType()?.code)
        validateParticipantIdsInPrivateDialog(entity.getParticipantIds(), type)
        validateNameInGroupAndPublicDialog(entity.getName(), type)

        dto.participantIds = entity.getParticipantIds()
        dto.type = convertIntFrom(entity.getType())
        dto.name = entity.getName()
        dto.id = entity.getDialogId()
        dto.ownerId = entity.getOwnerId()
        dto.updatedAt = entity.getUpdatedAt()
        dto.lastMessageText = entity.getLastMessage()?.getContent()
        dto.lastMessageDateSent = entity.getLastMessage()?.getTime() ?: 0
        dto.lastMessageUserId = entity.getLastMessage()?.getSenderId()
        dto.unreadMessageCount = entity.getUnreadMessagesCount()
        dto.photo = entity.getPhoto()
        return dto
    }

    private fun convertIntFrom(type: DialogEntity.Types?): Int? {
        return when (type) {
            DialogEntity.Types.PUBLIC -> 1
            DialogEntity.Types.GROUP -> 2
            DialogEntity.Types.PRIVATE -> 3
            else -> {
                null
            }
        }
    }

    @Throws(MappingException::class)
    private fun validateTypeDialog(type: Int?): Int {
        return type ?: throw MappingException("Dialog type can't be null")
    }

    @Throws(MappingException::class)
    private fun validateParticipantIdsInPrivateDialog(participantIds: Collection<Int>?, type: Int?) {
        val isPrivateType = type == DialogEntity.Types.PRIVATE.code
        val isParticipantIdsNullOrEmpty = participantIds.isNullOrEmpty()

        if (isPrivateType && isParticipantIdsNullOrEmpty) {
            throw MappingException("ParticipantIds can't be null or empty")
        }
    }

    @Throws(MappingException::class)
    private fun validateNameInGroupAndPublicDialog(name: String?, type: Int?) {
        val isGroupOrPublicType = type == DialogEntity.Types.GROUP.code || type == DialogEntity.Types.PUBLIC.code
        val isNameNullOrEmpty = name.isNullOrEmpty()

        if (isGroupOrPublicType && isNameNullOrEmpty) {
            throw MappingException("Dialog name can't be null or empty")
        }
    }

    fun toEntity(dto: LocalDialogDTO): DialogEntity {
        val entity = DialogEntityImpl()

        val type = validateTypeDialog(dto.type)
        validateParticipantIdsInPrivateDialog(dto.participantIds, dto.type)
        validateNameInGroupAndPublicDialog(dto.name, dto.type)

        entity.setDialogType(convertTypeToInt(type))
        entity.setName(dto.name)
        entity.setParticipantIds(dto.participantIds)
        entity.setDialogId(dto.id)
        entity.setOwnerId(dto.ownerId)
        entity.setUpdatedAt(dto.updatedAt)

        val lastMessageEntity = buildsLastMessageFrom(dto)
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(dto.unreadMessageCount)
        entity.setPhoto(dto.photo)
        entity.setIsOwner(dto.isOwner)
        return entity
    }

    fun toEntity(dto: RemoteDialogDTO): DialogEntity {
        val entity = DialogEntityImpl()

        val type = validateTypeDialog(dto.type)
        validateParticipantIdsInPrivateDialog(dto.participantIds, dto.type)
        validateNameInGroupAndPublicDialog(dto.name, dto.type)

        entity.setDialogType(convertTypeToInt(type))
        entity.setName(dto.name)
        entity.setParticipantIds(dto.participantIds)
        entity.setDialogId(dto.id)
        entity.setOwnerId(dto.ownerId)
        entity.setUpdatedAt(dto.updatedAt)

        val lastMessageEntity = buildsLastMessageFrom(dto)
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(dto.unreadMessageCount)
        entity.setPhoto(dto.photo)
        entity.setIsOwner(dto.isOwner)
        return entity
    }

    private fun convertTypeToInt(type: Int?): DialogEntity.Types? {
        return when (type) {
            1 -> DialogEntity.Types.PUBLIC
            2 -> DialogEntity.Types.GROUP
            3 -> DialogEntity.Types.PRIVATE
            else -> {
                null
            }
        }
    }

    private fun buildsLastMessageFrom(dto: LocalDialogDTO): IncomingChatMessageEntity {
        val contentType = parseContentTypeFrom(dto.lastMessageText)
        val entity = IncomingChatMessageEntityImpl(contentType)

        if (entity.isMediaContent()) {
            val mediaContent = parseMediaContentFrom(dto.lastMessageText)
            entity.setMediaContent(mediaContent)
        }

        entity.setContent(dto.lastMessageText)
        entity.setDialogId(dto.id)
        entity.setParticipantId(dto.lastMessageUserId)
        entity.setTime(dto.lastMessageDateSent)

        return entity
    }

    private fun buildsLastMessageFrom(dto: RemoteDialogDTO): IncomingChatMessageEntity {
        val contentType = parseContentTypeFrom(dto.lastMessageText)
        val entity = IncomingChatMessageEntityImpl(contentType)

        if (entity.isMediaContent()) {
            val mediaContent = parseMediaContentFrom(dto.lastMessageText)
            entity.setMediaContent(mediaContent)
        }

        entity.setContent(dto.lastMessageText)
        entity.setDialogId(dto.id)
        entity.setParticipantId(dto.lastMessageUserId)
        entity.setTime(dto.lastMessageDateSent)
        entity.setSenderId(dto.ownerId)

        return entity
    }

    private fun parseContentTypeFrom(messageText: String?): ChatMessageEntity.ContentTypes {
        val isMedia = isContainsMediaIn(messageText)

        if (isMedia) {
            return ChatMessageEntity.ContentTypes.MEDIA
        } else {
            return ChatMessageEntity.ContentTypes.TEXT
        }
    }

    private fun isContainsMediaIn(messageText: String?): Boolean {
        return messageText?.contains(MediaContentEntity::class.java.simpleName) == true
    }

    private fun parseMediaContentFrom(messageText: String?): MediaContentEntity {
        val splitText = messageText?.split("|")
        val fileName = splitText?.get(1) ?: ""
        val fileUrl = splitText?.get(2) ?: ""
        val fileMimeType = splitText?.get(3) ?: ""

        return MediaContentEntityImpl(fileName, fileUrl, fileMimeType)
    }

    fun localDTOFrom(dialogId: String): LocalDialogDTO {
        val dto = LocalDialogDTO()
        dto.id = dialogId
        return dto
    }

    fun remoteDTOFrom(dialogId: String): RemoteDialogDTO {
        val dto = RemoteDialogDTO()
        dto.id = dialogId
        return dto
    }

    fun entitiesFrom(localDTO: LocalDialogsDTO): List<DialogEntity> {
        val entities = arrayListOf<DialogEntity>()

        val dialogs = localDTO.dialogs
        dialogs?.let {
            for (dto in dialogs) {
                val entity = toEntity(dto)
                entities.add(entity)
            }
        }
        return entities
    }
}