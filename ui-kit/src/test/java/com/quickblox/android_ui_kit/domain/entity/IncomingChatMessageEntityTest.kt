/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.spy.entity.UserEntitySpy
import junit.framework.Assert.*
import org.junit.Test
import kotlin.random.Random

class IncomingChatMessageEntityTest {
    // read tests
    @Test
    fun buildEntity_loggedIdExistInReadIds_isNotReadReturnFalse() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)
        entity.setReadIds(mutableListOf(777, 888))

        assertFalse(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotExistInReadIds_isNotReadReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)
        entity.setReadIds(mutableListOf(777))

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotSet_isNotReadReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setReadIds(mutableListOf(777))

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_ReadIdsNotSet_isNotReadReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotSetAndReadIdsNotSet_isNotReadReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        assertTrue(entity.isNotRead())
    }

    // delivered tests
    @Test
    fun buildEntity_loggedIdExistInDeliveredIds_isNotDeliveredReturnFalse() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)
        entity.setDeliveredIds(mutableListOf(777, 888))

        assertFalse(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotExistInDeliveredIds_isNotDeliveredReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)
        entity.setDeliveredIds(mutableListOf(777))

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotSet_isNotDeliveredReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setDeliveredIds(mutableListOf(777))

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_deliveredIdsNotSet_isNotDeliveredReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setLoggedUserId(888)

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotSetAndDeliveredIdsNotSet_isNotDeliveredReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        assertTrue(entity.isNotDelivered())
    }

    // message type tests
    @Test
    fun buildEntity_setContentText_isMediaContentReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        val messageType = entity.getMessageType()
        assertEquals(MessageEntity.MessageTypes.CHAT, messageType)
    }

    // media content tests
    @Test
    fun buildEntity_setContentTypeMedia_isMediaContentReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.MEDIA)
        assertTrue(entity.isMediaContent())
    }

    @Test
    fun buildEntity_setContentTypeText_isMediaContentReturnFalse() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        assertFalse(entity.isMediaContent())
    }

    // equals tests
    @Test
    fun buildEntity_setFields_fieldsEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()
        val sender = UserEntitySpy()

        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entity.setParticipantId(participantId)
        entity.setSenderId(senderId)
        entity.setDialogId(dialogId)
        entity.setMessageId(messageId)
        entity.setSender(sender)

        assertEquals(participantId, entity.getParticipantId())
        assertEquals(senderId, entity.getSenderId())
        assertEquals(dialogId, entity.getDialogId())
        assertEquals(messageId, entity.getMessageId())
        assertEquals(sender, entity.getSender())
        assertEquals(ChatMessageEntity.ChatMessageTypes.FROM_OPPONENT, entity.getChatMessageType())
    }

    @Test
    fun build2Entity_setFields_entitiesAreEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()

        val entityA = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entityA.setParticipantId(participantId)
        entityA.setSenderId(senderId)
        entityA.setDialogId(dialogId)
        entityA.setMessageId(messageId)

        val entityB = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        entityB.setParticipantId(participantId)
        entityB.setSenderId(senderId)
        entityB.setDialogId(dialogId)
        entityB.setMessageId(messageId)

        assertTrue(entityA == entityB)
        assertTrue(entityA.hashCode() == entityB.hashCode())
    }

    @Test
    fun build2Entity_setFields_entitiesNotEquals() {
        val entityA = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        val entityB = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)

        assertTrue(entityA != entityB)
        assertTrue(entityA.hashCode() != entityB.hashCode())
    }
}