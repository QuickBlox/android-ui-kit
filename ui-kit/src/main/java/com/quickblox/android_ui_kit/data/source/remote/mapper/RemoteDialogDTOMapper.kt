/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote.mapper

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogsDTO
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import java.text.SimpleDateFormat
import java.util.*

object RemoteDialogDTOMapper {
    @Throws(MappingException::class)
    fun toDTOFrom(dialogs: ArrayList<QBChatDialog>, loggedUserId: Int?): RemoteDialogsDTO {
        val dialogsDTO = dialogsDTOFrom(dialogs, loggedUserId)
        val dto = RemoteDialogsDTO()
        dto.dialogs = dialogsDTO
        return dto
    }

    @Throws(MappingException::class)
    fun toDTOFrom(dialog: QBChatDialog, loggedUserId: Int?): RemoteDialogDTO {
        return dialogToDTO(dialog, loggedUserId)
    }

    @Throws(MappingException::class)
    fun toQBDialogFrom(remoteDialogDTO: RemoteDialogDTO): QBChatDialog {
        val chatDialog = QBChatDialog()

        val type = getDialogTypeFrom(remoteDialogDTO.type)
        validateParticipantIdsInPrivateDialog(remoteDialogDTO.participantIds, type)
        validateNameInGroupAndPublicDialog(remoteDialogDTO.name, type)

        chatDialog.type = type
        chatDialog.setOccupantsIds(remoteDialogDTO.participantIds?.toList())
        chatDialog.name = remoteDialogDTO.name
        chatDialog.dialogId = remoteDialogDTO.id
        chatDialog.userId = remoteDialogDTO.ownerId

        val updatedAt = dateFrom(remoteDialogDTO.updatedAt)
        chatDialog.updatedAt = updatedAt
        chatDialog.lastMessage = remoteDialogDTO.lastMessageText
        remoteDialogDTO.lastMessageDateSent?.let {
            chatDialog.lastMessageDateSent = it
        }
        chatDialog.lastMessageUserId = remoteDialogDTO.lastMessageUserId
        chatDialog.unreadMessageCount = remoteDialogDTO.unreadMessageCount
        chatDialog.photo = remoteDialogDTO.photo
        return chatDialog
    }

    @Throws(MappingException::class)
    private fun getDialogTypeFrom(type: Int?): QBDialogType {
        val dialogType = when (type) {
            1 -> QBDialogType.PUBLIC_GROUP
            2 -> QBDialogType.GROUP
            3 -> QBDialogType.PRIVATE
            else -> {
                null
            }
        }
        return dialogType ?: throw MappingException("Dialog type can't be null")
    }

    fun getDialogIdFrom(remoteDialogDTO: RemoteDialogDTO): String {
        return remoteDialogDTO.id ?: ""
    }

    private fun dialogsDTOFrom(dialogs: ArrayList<QBChatDialog>, ownerId: Int?): ArrayList<RemoteDialogDTO> {
        val dialogsDTO = arrayListOf<RemoteDialogDTO>()
        dialogs.forEach { dialog ->
            val dto = dialogToDTO(dialog, ownerId)
            dialogsDTO.add(dto)
        }
        return dialogsDTO
    }

    @Throws(MappingException::class)
    private fun dialogToDTO(dialog: QBChatDialog, loggedUserId: Int?): RemoteDialogDTO {
        val dto = RemoteDialogDTO()

        val type = getDialogType(dialog.type)
        validateParticipantIdsInPrivateDialog(dialog.occupants, type)
        validateNameInGroupAndPublicDialog(dialog.name, type)

        val isParticipantIdsNotNullOrEmpty = !dialog.occupants.isNullOrEmpty()
        if (isParticipantIdsNotNullOrEmpty) {
            dto.participantIds = dialog.occupants
        }

        dto.name = dialog.name
        dto.type = dialog.type?.code
        dto.id = dialog.dialogId
        dto.ownerId = dialog.userId
        val updatedAt = stringFrom(dialog.updatedAt)
        dto.updatedAt = updatedAt
        dto.lastMessageText = dialog.lastMessage
        dto.lastMessageDateSent = dialog.lastMessageDateSent
        dto.lastMessageUserId = dialog.lastMessageUserId
        dto.unreadMessageCount = dialog.unreadMessageCount
        dto.isOwner = isOwner(dto.ownerId, loggedUserId)

        val isPhotoUrlValid = !dialog.photo.isNullOrEmpty() && dialog.photo != "null"
        if (isPhotoUrlValid) {
            dto.photo = dialog.photo
        }
        return dto
    }

    private fun isOwner(ownerId: Int?, loggedUserId: Int?): Boolean {
        return ownerId == loggedUserId
    }

    private fun getDialogType(type: QBDialogType?): QBDialogType {
        return type ?: throw MappingException("Dialog type can't be null")
    }

    @Throws(MappingException::class)
    private fun validateParticipantIdsInPrivateDialog(participantIds: Collection<Int>?, type: QBDialogType) {
        val isPrivateType = type == QBDialogType.PRIVATE
        val isParticipantIdsNullOrEmpty = participantIds.isNullOrEmpty()

        if (isPrivateType && isParticipantIdsNullOrEmpty) {
            throw MappingException("ParticipantIds can't be null or empty")
        }
    }

    @Throws(MappingException::class)
    private fun validateNameInGroupAndPublicDialog(name: String?, type: QBDialogType) {
        val isGroupOrPublicType = type == QBDialogType.PUBLIC_GROUP || type == QBDialogType.GROUP
        val isNameNullOrEmpty = name.isNullOrEmpty()

        if (isGroupOrPublicType && isNameNullOrEmpty) {
            throw MappingException("Dialog name can't be null or empty")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stringFrom(date: Date?): String? {
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH)
        return date?.let { format.format(it) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun dateFrom(date: String?): Date? {
        val parser = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH)
        return date?.let {
            parser.parse(it)
        }
    }
}