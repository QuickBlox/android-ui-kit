/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.listener

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteMessageDTOMapper
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.chat.listeners.QBMessageStatusListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class StatusMessageListener(private val messagesEventFlow: MutableSharedFlow<RemoteMessageDTO?>) :
    QBMessageStatusListener {
    private var TAG = StatusMessageListener::javaClass.name

    override fun processMessageDelivered(messageId: String, dialogId: String, senderId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isNotFromLoggedUser(senderId)) {
                val remoteMessageDTO = buildDeliveredRemoteMessage(messageId, dialogId, senderId)
                messagesEventFlow.emit(remoteMessageDTO)
            }
        }
    }

    override fun processMessageRead(messageId: String, dialogId: String, senderId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isNotFromLoggedUser(senderId)) {
                val remoteMessageDTO = buildReadRemoteMessage(messageId, dialogId, senderId)
                messagesEventFlow.emit(remoteMessageDTO)
            }
        }
    }

    private fun isNotFromLoggedUser(senderId: Int): Boolean {
        return getLoggedUserIdFromSession() != senderId
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

    private fun buildDeliveredRemoteMessage(messageId: String, dialogId: String, senderId: Int): RemoteMessageDTO {
        val remoteMessageDTO = RemoteMessageDTOMapper.messageDTOWithDeliveredStatus(messageId, dialogId, senderId)
        return remoteMessageDTO
    }

    private fun buildReadRemoteMessage(messageId: String, dialogId: String, senderId: Int): RemoteMessageDTO {
        val remoteMessageDTO = RemoteMessageDTOMapper.messageDTOWithReadStatus(messageId, dialogId, senderId)
        return remoteMessageDTO
    }

    override fun equals(other: Any?): Boolean {
        return if (other is StatusMessageListener) {
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