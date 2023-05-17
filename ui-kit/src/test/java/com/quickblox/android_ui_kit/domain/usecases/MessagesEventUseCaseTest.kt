/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.repository.mapper.MessageMapper
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MessagesEventUseCaseTest : BaseTest() {
    @Before
    @ExperimentalCoroutinesApi
    fun init() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    @ExperimentalCoroutinesApi
    fun release() {
        Dispatchers.resetMain()
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun noErrors_executeAndSendEventTwoMessages_receivedTwoMessages() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val messageLatch = CountDownLatch(2)
        val dialog = DialogEntitySpy()
        val executeScope = launch(UnconfinedTestDispatcher()) {
            MessagesEventUseCase(dialog).execute().collect {
                messageLatch.countDown()
            }
        }

        CountDownLatch(1).await(2, TimeUnit.SECONDS)

        eventsRepository.sendMessage(buildIncomingMessageWithDialogId(dialog.getDialogId()))
        eventsRepository.sendMessage(buildIncomingMessageWithDialogId(dialog.getDialogId()))

        assertEquals(0, messageLatch.count)

        executeScope.cancel()
    }

    private fun buildIncomingMessageWithDialogId(dialogId: String?): MessageEntity {
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val message = IncomingChatMessageEntityImpl(contentType)
        message.setContent(System.currentTimeMillis().toString())
        message.setDialogId(dialogId)

        return message
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun noErrors_executeAndSendReadMessage_receivedMessage() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val messageLatch = CountDownLatch(1)
        val dialog = DialogEntitySpy()

        var receivedMessage: OutgoingChatMessageEntity? = null
        val executeScope = launch(UnconfinedTestDispatcher()) {
            MessagesEventUseCase(dialog).execute().collect { messageEntity ->
                receivedMessage = messageEntity as OutgoingChatMessageEntity
                messageLatch.countDown()
            }
        }

        CountDownLatch(1).await(2, TimeUnit.SECONDS)

        eventsRepository.sendMessage(makeRemoteMessageDTOWithReadStatus(dialog.getDialogId()))

        assertEquals(0, messageLatch.count)
        assertEquals(dialog.getDialogId(), receivedMessage!!.getDialogId())
        assertTrue(receivedMessage?.getTime()!! > 0)

        executeScope.cancel()
    }

    private fun makeRemoteMessageDTOWithReadStatus(dialogId: String?): MessageEntity {
        val dto = RemoteMessageDTO()
        dto.id = System.currentTimeMillis().toString()
        dto.dialogId = dialogId
        dto.outgoing = true
        dto.senderId = Random.nextInt(1000, 10000)
        dto.type = RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        dto.time = System.currentTimeMillis() / 1000
        dto.outgoingState = RemoteMessageDTO.OutgoingMessageStates.READ

        return MessageMapper.outgoingChatEntityFrom(dto)
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun noErrors_executeAndSendDeliveredMessage_receivedMessage() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val messageLatch = CountDownLatch(1)
        val dialog = DialogEntitySpy()

        var receivedMessage: OutgoingChatMessageEntity? = null
        val executeScope = launch(UnconfinedTestDispatcher()) {
            MessagesEventUseCase(dialog).execute().collect { messageEntity ->
                receivedMessage = messageEntity as OutgoingChatMessageEntity
                messageLatch.countDown()
            }
        }

        CountDownLatch(1).await(2, TimeUnit.SECONDS)

        eventsRepository.sendMessage(makeRemoteMessageDTOWithDeliveredStatus(dialog.getDialogId()))

        assertEquals(0, messageLatch.count)
        assertEquals(dialog.getDialogId(), receivedMessage!!.getDialogId())
        assertTrue(receivedMessage?.getTime()!! > 0)

        executeScope.cancel()
    }

    private fun makeRemoteMessageDTOWithDeliveredStatus(dialogId: String?): MessageEntity {
        val dto = RemoteMessageDTO()
        dto.id = System.currentTimeMillis().toString()
        dto.dialogId = dialogId
        dto.outgoing = true
        dto.senderId = Random.nextInt(1000, 10000)
        dto.type = RemoteMessageDTO.MessageTypes.CHAT_MESSAGE
        dto.time = System.currentTimeMillis() / 1000
        dto.outgoingState = RemoteMessageDTO.OutgoingMessageStates.DELIVERED

        return MessageMapper.outgoingChatEntityFrom(dto)
    }
}