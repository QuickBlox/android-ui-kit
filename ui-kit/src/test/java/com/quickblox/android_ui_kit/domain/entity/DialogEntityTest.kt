/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteDialogDTOMapper
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.GROUP
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.PRIVATE
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.spy.entity.CustomDataEntitySpy
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.random.Random

class DialogEntityTest {
    @Test
    fun buildDialogEntity_IsOwnerSetTrue_IsOwnerReturnTrue() {
        val entity = DialogEntityImpl()
        entity.setIsOwner(true)

        assertTrue(entity.isOwner()!!)
    }

    @Test
    fun buildDialogEntity_IsOwnerSetFalse_IsOwnerReturnFalse() {
        val entity = DialogEntityImpl()
        entity.setIsOwner(false)

        assertFalse(entity.isOwner()!!)
    }

    @Test
    fun buildDialogEntity_SetCustomData_CustomDataAreEquals() {
        val customData = CustomDataEntitySpy()

        val entity = DialogEntityImpl()
        entity.setCustomData(customData)

        assertEquals(customData, entity.getCustomData())
    }

    @Test
    fun build2DialogsEntityWithSameDialogId_GetHashCode_DialogsAndHashCodesAreEquals() {
        val dialogId = System.currentTimeMillis().toString()

        val entityA = DialogEntityImpl()
        entityA.setDialogId(dialogId)

        val entityB = DialogEntityImpl()
        entityB.setDialogId(dialogId)

        assertTrue(entityA == entityB)
        assertTrue(entityA.hashCode() == entityB.hashCode())
    }

    @Test
    fun build2DialogsEntityWithoutDialogId_GetHashCode_DialogsAndHashCodesNotEquals() {
        val entityA = DialogEntityImpl()
        val entityB = DialogEntityImpl()

        assertTrue(entityA != entityB)
        assertTrue(entityA.hashCode() != entityB.hashCode())
    }

    @Test
    fun buildGroupDialogEntityStub_CompareEntities_EqualsTrue() {
        val entity = buildDialogEntityStub()
        val otherEntity = buildDialogEntityStub()

        assertTrue(entity == otherEntity)
        assertTrue(entity.hashCode() == otherEntity.hashCode())
    }

    @Test
    fun buildRandomDialogEntityStub_CompareEntities_EqualsFalse() {
        val entity = buildRandomDialogEntityStub()
        val otherEntity = buildRandomDialogEntityStub()

        assertFalse(entity == otherEntity)
        assertFalse(entity.hashCode() == otherEntity.hashCode())
    }

    private fun buildDialogEntityStub(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogId("test_id")
        entity.setName("test_name")
        entity.setDialogType(PRIVATE)
        entity.setOwnerId(111111)
        entity.setUpdatedAt("test_time")

        val lastMessageEntity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        lastMessageEntity.setContent("Last Message Text")
        lastMessageEntity.setTime(1234L)
        lastMessageEntity.setSenderId(111111)
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(2)
        entity.setName("Dialog Stub ")
        entity.setPhoto("photo")
        return entity
    }

    private fun buildRandomDialogEntityStub(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogId(UUID.randomUUID().toString())
        entity.setDialogType(GROUP)
        entity.setOwnerId(Random.nextInt(0, 1000))

        val currentTime = RemoteDialogDTOMapper.stringFrom(Calendar.getInstance().time)
        entity.setUpdatedAt(currentTime)

        val lastMessageEntity = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        lastMessageEntity.setContent("Last Message Text ${Random.nextInt(0, 1000)}")

        val randomLong = Random.nextLong(0, Long.MAX_VALUE)
        lastMessageEntity.setTime(randomLong)
        lastMessageEntity.setSenderId(Random.nextInt(0, 1000))
        entity.setLastMessage(lastMessageEntity)
        entity.setUnreadMessagesCount(Random.nextInt(0, 10))
        entity.setName("Dialog Stub ${Random.nextInt(0, 1000)}")
        entity.setPhoto(" ${Random.nextInt(0, 1000)}")
        return entity
    }
}