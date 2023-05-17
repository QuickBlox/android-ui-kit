/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.listener

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.source.remote.parser.EventMessageParser
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SystemMessageListener(private val dialogsEventFlow: MutableSharedFlow<RemoteDialogDTO?>) :
    QBSystemMessageListener {
    private var TAG = SystemMessageListener::javaClass.name

    override fun processMessage(qbChatMessage: QBChatMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            if (EventMessageParser.isEventFrom(qbChatMessage) && isNotFromLoggedUser(qbChatMessage)) {
                val remoteDialogDTO = getRemoteDialogDtoBy(qbChatMessage.dialogId)
                dialogsEventFlow.emit(remoteDialogDTO)
            }
        }
    }

    private fun isNotFromLoggedUser(qbChatMessage: QBChatMessage): Boolean {
        return getLoggedUserIdFromSession() != qbChatMessage.senderId
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

    private fun getRemoteDialogDtoBy(dialogId: String): RemoteDialogDTO {
        val remoteDialogDTO = RemoteDialogDTO()
        remoteDialogDTO.id = dialogId

        return remoteDialogDTO
    }

    override fun equals(other: Any?): Boolean {
        return if (other is SystemMessageListener) {
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

    override fun processError(p0: QBChatException?, p1: QBChatMessage?) {
        // TODO: need add logic for handle error
    }
}