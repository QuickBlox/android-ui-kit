/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.listener

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.chat.listeners.QBChatDialogTypingListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class TypingMessageListener(private val messagesEventFlow: MutableSharedFlow<RemoteMessageDTO?>) :
    QBChatDialogTypingListener {
    private var TAG = TypingMessageListener::javaClass.name

    override fun processUserIsTyping(dialogId: String?, userId: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            val messageDTO = buildStartTypingRemoteMessage()
            messagesEventFlow.emit(messageDTO)
        }
    }

    override fun processUserStopTyping(dialogId: String?, userId: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            val messageDTO = buildStopTypingRemoteMessage()
            messagesEventFlow.emit(messageDTO)
        }
    }

    private fun buildStartTypingRemoteMessage(): RemoteMessageDTO {
        // TODO: need to add login
        return RemoteMessageDTO()
    }

    private fun buildStopTypingRemoteMessage(): RemoteMessageDTO {
        // TODO: need to add login
        return RemoteMessageDTO()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is TypingMessageListener) {
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