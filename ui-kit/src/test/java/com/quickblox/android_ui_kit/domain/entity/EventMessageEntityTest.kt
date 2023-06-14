/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.message.EventMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import junit.framework.Assert.*
import org.junit.Test
import kotlin.random.Random

class EventMessageEntityTest {
    // read tests
    @Test
    fun buildEntity_loggedIdExistInReadIds_isNotReadReturnFalse() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)
        entity.setReadIds(mutableListOf(777, 888))

        assertFalse(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotExistInReadIds_isNotReadReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)
        entity.setReadIds(mutableListOf(777))

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotSet_isNotReadReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setReadIds(mutableListOf(777))

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_ReadIdsNotSet_isNotReadReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)

        assertTrue(entity.isNotRead())
    }

    @Test
    fun buildEntity_loggedIdNotSetAndReadIdsNotSet_isNotReadReturnTrue() {
        val entity = EventMessageEntityImpl()
        assertTrue(entity.isNotRead())
    }

    // delivered tests
    @Test
    fun buildEntity_loggedIdExistInDeliveredIds_isNotDeliveredReturnFalse() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)
        entity.setDeliveredIds(mutableListOf(777, 888))

        assertFalse(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotExistInDeliveredIds_isNotDeliveredReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)
        entity.setDeliveredIds(mutableListOf(777))

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotSet_isNotDeliveredReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setDeliveredIds(mutableListOf(777))

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_deliveredIdsNotSet_isNotDeliveredReturnTrue() {
        val entity = EventMessageEntityImpl()
        entity.setLoggedUserId(888)

        assertTrue(entity.isNotDelivered())
    }

    @Test
    fun buildEntity_loggedIdNotSetAndDeliveredIdsNotSet_isNotDeliveredReturnTrue() {
        val entity = EventMessageEntityImpl()
        assertTrue(entity.isNotDelivered())
    }

    // message type tests
    @Test
    fun buildEntity_setContentText_isMediaContentReturnTrue() {
        val entity = EventMessageEntityImpl()
        val messageType = entity.getMessageType()
        assertEquals(MessageEntity.MessageTypes.EVENT, messageType)
    }

    // media content tests
    @Test
    fun buildEntity_setContentTypeMedia_isMediaContentReturnTrue() {
        val entity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.MEDIA)
        assertTrue(entity.isMediaContent())
    }

    // equals tests
    @Test
    fun buildEntity_setFields_fieldsEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()
        val time = System.currentTimeMillis()
        val text = System.currentTimeMillis().toString()

        val entity = EventMessageEntityImpl()
        entity.setParticipantId(participantId)
        entity.setSenderId(senderId)
        entity.setDialogId(dialogId)
        entity.setMessageId(messageId)
        entity.setTime(time)
        entity.setText(text)

        assertEquals(participantId, entity.getParticipantId())
        assertEquals(senderId, entity.getSenderId())
        assertEquals(dialogId, entity.getDialogId())
        assertEquals(messageId, entity.geMessageId())
        assertEquals(time, entity.getTime())
        assertEquals(text, entity.getText())
    }

    @Test
    fun build2Entity_setFields_entitiesAreEquals() {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()

        val entityA = EventMessageEntityImpl()
        entityA.setParticipantId(participantId)
        entityA.setSenderId(senderId)
        entityA.setDialogId(dialogId)
        entityA.setMessageId(messageId)

        val entityB = EventMessageEntityImpl()
        entityB.setParticipantId(participantId)
        entityB.setSenderId(senderId)
        entityB.setDialogId(dialogId)
        entityB.setMessageId(messageId)

        assertTrue(entityA == entityB)
        assertTrue(entityA.hashCode() == entityB.hashCode())
    }

    @Test
    fun build2Entity_setFields_entitiesNotEquals() {
        val entityA = EventMessageEntityImpl()
        val entityB = EventMessageEntityImpl()

        assertTrue(entityA != entityB)
        assertTrue(entityA.hashCode() != entityB.hashCode())
    }
}