/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.listener

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteMessageDTOMapper
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.chat.QBChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ChatMessageListener(
    private val messagesEventFlow: MutableSharedFlow<RemoteMessageDTO?>,
    private val dialogsEventFlow: MutableSharedFlow<RemoteDialogDTO?>
) : QBChatDialogMessageListener {
    private var TAG = ChatMessageListener::javaClass.name

    override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isNotFromCarbon() && isNotFromLoggedUser(qbChatMessage)) {
                val remoteMessageDTO = buildRemoteMessageDTO(qbChatMessage)
                messagesEventFlow.emit(remoteMessageDTO)
            }

            if (isEventFromLoggedUser(qbChatMessage)) {
                val remoteMessageDTO = buildRemoteMessageDTO(qbChatMessage)
                messagesEventFlow.emit(remoteMessageDTO)
            }

            if (EventMessageParser.isNotEventFrom(qbChatMessage)) {
                val remoteDialogDTO = buildRemoteDialogDTOFrom(qbChatMessage)
                dialogsEventFlow.emit(remoteDialogDTO)
            }
        }
    }

    private fun isNotFromCarbon(): Boolean {
        // TODO: Need to add logic, to handle Carbone messages from logged user
        return true
    }

    private fun isNotFromLoggedUser(qbChatMessage: QBChatMessage): Boolean {
        return getLoggedUserIdFromSession() != qbChatMessage.senderId
    }

    private fun isEventFromLoggedUser(qbChatMessage: QBChatMessage): Boolean {
        val isFromLoggedUser = getLoggedUserIdFromSession() == qbChatMessage.senderId
        return isFromLoggedUser && EventMessageParser.isEventFrom(qbChatMessage)
    }

    private fun getLoggedUserIdFromSession(): Int? {
        val userIdFromSession = QBSessionManager.getInstance().activeSession?.userId ?: 0
        if (userIdFromSession > 0) {
            return userIdFromSession
        }

        val userIdFromSessionParameters = QBSessionManager.getInstance().sessionParameters?.userId ?: 0
        if (userIdFromSessionParameters > 0) {
            return userIdFromSessionParameters
        }
        return null
    }

    private fun buildRemoteMessageDTO(qbChatMessage: QBChatMessage): RemoteMessageDTO {
        val loggedUserId = QBChatService.getInstance().user.id
        val remoteMessageDTO = RemoteMessageDTOMapper.messageDTOFrom(qbChatMessage, loggedUserId)

        if (isExistAttachmentIn(qbChatMessage)) {
            val attachment = qbChatMessage.attachments.toList()[0]
            remoteMessageDTO.fileUrl = attachment?.url
            remoteMessageDTO.mimeType = attachment?.contentType
        }
        return remoteMessageDTO
    }

    private fun buildRemoteDialogDTOFrom(qbChatMessage: QBChatMessage): RemoteDialogDTO {
        val loggedUserId = QBChatService.getInstance().user.id
        val remoteMessageDTO = RemoteMessageDTOMapper.messageDTOFrom(qbChatMessage, loggedUserId)

        val remoteDialogDTO = RemoteDialogDTO()
        remoteDialogDTO.id = remoteMessageDTO.dialogId
        remoteDialogDTO.lastMessageText = remoteMessageDTO.text
        remoteDialogDTO.lastMessageDateSent = qbChatMessage.dateSent

        return remoteDialogDTO
    }

    private fun isExistAttachmentIn(qbChatMessage: QBChatMessage): Boolean {
        return qbChatMessage.attachments != null && qbChatMessage.attachments.isNotEmpty()
    }

    override fun processError(
        dialogId: String?,
        exception: QBChatException?,
        qbChatMessage: QBChatMessage?,
        userId: Int?
    ) {
        // TODO: need add logic for handle error
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ChatMessageListener) {
            TAG == other.TAG
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + TAG.hashCode()
        return hash
    }
}