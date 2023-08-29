/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import junit.framework.Assert.*
import org.junit.Test
import kotlin.random.Random

class OutgoingChatMessageEntityTest {
    private val readState = OutgoingChatMessageEntity.OutgoingStates.READ
    private val textType = ChatMessageEntity.ContentTypes.TEXT

    // message type tests
    @Test
    fun buildEntity_setContentText_isMediaContentReturnTrue() {
        val entity = OutgoingChatMessageEntityImpl(readState, textType)
        val messageType = entity.getMessageType()

        assertEquals(MessageEntity.MessageTypes.CHAT, messageType)
    }

    // media content tests
    @Test
    fun buildEntity_setContentTypeMedia_isMediaContentReturnTrue() {
        val entity = OutgoingChatMessageEntityImpl(readState, ChatMessageEntity.ContentTypes.MEDIA)
        assertTrue(entity.isMediaContent())
    }

    @Test
    fun buildEntity_setContentTypeText_isMediaContentReturnFalse() {
        val entity = OutgoingChatMessageEntityImpl(readState, textType)
        assertFalse(entity.isMediaContent())
    }

    // equals tests
    @Test
    fun buildEntity_setFields_fieldsEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()
        val time = System.currentTimeMillis()

        val entity = OutgoingChatMessageEntityImpl(readState, textType)
        entity.setParticipantId(participantId)
        entity.setSenderId(senderId)
        entity.setDialogId(dialogId)
        entity.setMessageId(messageId)
        entity.setTime(time)

        assertEquals(participantId, entity.getParticipantId())
        assertEquals(senderId, entity.getSenderId())
        assertEquals(dialogId, entity.getDialogId())
        assertEquals(messageId, entity.getMessageId())
        assertEquals(time, entity.getTime())
        assertEquals(readState, entity.getOutgoingState())
        assertEquals(ChatMessageEntity.ChatMessageTypes.FROM_LOGGED_USER, entity.getChatMessageType())
    }

    @Test
    fun build2Entity_setFields_entitiesAreEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()

        val entityA = OutgoingChatMessageEntityImpl(readState, textType)
        entityA.setParticipantId(participantId)
        entityA.setSenderId(senderId)
        entityA.setDialogId(dialogId)
        entityA.setMessageId(messageId)

        val entityB = OutgoingChatMessageEntityImpl(readState, textType)
        entityB.setParticipantId(participantId)
        entityB.setSenderId(senderId)
        entityB.setDialogId(dialogId)
        entityB.setMessageId(messageId)

        assertTrue(entityA == entityB)
        assertTrue(entityA.hashCode() == entityB.hashCode())
    }

    @Test
    fun build2Entity_setFields_entitiesNotEquals() {
        val entityA = OutgoingChatMessageEntityImpl(readState, textType)
        val entityB = OutgoingChatMessageEntityImpl(readState, textType)

        assertTrue(entityA != entityB)
        assertTrue(entityA.hashCode() != entityB.hashCode())
    }
}