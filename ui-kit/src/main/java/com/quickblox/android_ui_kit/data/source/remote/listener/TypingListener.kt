/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.listener

import com.quickblox.android_ui_kit.data.dto.remote.typing.RemoteTypingDTO
import com.quickblox.chat.JIDHelper
import com.quickblox.chat.QBChatService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jivesoftware.smack.StanzaListener
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.ChatStateManager
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension

open class TypingListener(
    private val typingEventFlow: MutableSharedFlow<RemoteTypingDTO?>,
    private val tag: String
) : StanzaListener {
    override fun processPacket(packet: Stanza?) {
        if (isChatStateExtensionExistIn(packet)) {
            val senderId = parseSenderIdFrom(packet)
            if (senderId <= 0 || isLoggedUserIdSame(senderId)) {
                return
            }

            val isStartedTypingState = getChatStateFrom(packet) === ChatState.composing
            val isStoppedTypingState = getChatStateFrom(packet) === ChatState.paused

            if (isStartedTypingState) {
                sendStartedTypingEvent(senderId)
            }

            if (isStoppedTypingState) {
                sendStoppedTypingEvent(senderId)
            }
        }
    }

    private fun isChatStateExtensionExistIn(packet: Stanza?): Boolean {
        val message: Message? = packet as Message?
        val chatStateExtension = message?.getExtension(ChatStateManager.NAMESPACE) as ChatStateExtension?
        return chatStateExtension != null
    }


    private fun parseSenderIdFrom(packet: Stanza?): Int {
        val message: Message? = packet as Message?

        when (message?.type) {
            Message.Type.chat -> {
                return parsePrivateChatSenderIdFrom(message)
            }
            Message.Type.groupchat -> {
                return parseGroupChatSenderIdFrom(message)
            }
            else -> {
                return -1
            }
        }
    }

    private fun parsePrivateChatSenderIdFrom(message: Message?): Int {
        return JIDHelper.INSTANCE.parseUserId(message?.from)
    }

    private fun parseGroupChatSenderIdFrom(message: Message?): Int {
        return JIDHelper.INSTANCE.parseRoomOccupant(message?.from)
    }

    private fun isLoggedUserIdSame(senderId: Int): Boolean {
        return senderId == QBChatService.getInstance().user.id
    }

    private fun getChatStateFrom(packet: Stanza?): ChatState {
        val chatStateExtension = getStateChatExtension(packet)
        val chatState = getChatStateFrom(chatStateExtension)
        return chatState
    }

    private fun getStateChatExtension(packet: Stanza?): ChatStateExtension {
        val message: Message? = packet as Message?
        return message?.getExtension(ChatStateManager.NAMESPACE) as ChatStateExtension
    }

    private fun getChatStateFrom(extensionElement: ExtensionElement): ChatState {
        val name = extensionElement.elementName
        val state = ChatState.valueOf(name)
        return state
    }

    private fun sendStartedTypingEvent(senderId: Int) {
        sendTypingEvent(senderId, RemoteTypingDTO.Types.STARTED)
    }

    private fun sendStoppedTypingEvent(senderId: Int) {
        sendTypingEvent(senderId, RemoteTypingDTO.Types.STOPPED)
    }

    private fun sendTypingEvent(senderId: Int, type: RemoteTypingDTO.Types) {
        val dto = RemoteTypingDTO()
        dto.senderId = senderId
        dto.type = type

        CoroutineScope(Dispatchers.IO).launch {
            typingEventFlow.emit(dto)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is TypingListener) {
            tag == other.tag
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + tag.hashCode()
        return hash
    }
}