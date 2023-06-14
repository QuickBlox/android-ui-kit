/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import junit.framework.Assert.*
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

class SendChatMessageUseCaseTest : BaseTest() {
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

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun createMediaMessageWithoutMediaContent_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.MEDIA)
        message.setDialogId("dummy_dialog_id")

        SendChatMessageUseCase(message).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun createMediaMessageWithoutDialogId_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.MEDIA)

        SendChatMessageUseCase(message).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createMediaContentEntity_makeMessageBodyFromMediaContent_messageBodyEquals() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = SendChatMessageUseCase(buildChatOutgoingMessage())

        val mediaContentEntity = MediaContentEntityImpl("dummy_file_name", "dummy_file_url", "dummy_mime_type")
        val messageBody = useCase.makeMessageBodyFromMediaContent(mediaContentEntity)
        assertEquals("MediaContentEntity|dummy_file_name|dummy_file_url|dummy_mime_type", messageBody)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessageEntityWithMediaContent_isMediaContentNotAvailableIn_receivedFalse() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = SendChatMessageUseCase(buildChatOutgoingMessage())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.MEDIA)
        message.setMediaContent(MediaContentEntityImpl("dummy_file_name", "dummy_file_url", "dummy_mime_type"))

        val mediaContentAvailable = useCase.isMediaContentNotAvailableIn(message)
        assertFalse(mediaContentAvailable)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessageEntityWithoutMediaContent_isMediaContentNotAvailableIn_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = SendChatMessageUseCase(buildChatOutgoingMessage())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT)

        val mediaContentAvailable = useCase.isMediaContentNotAvailableIn(message)
        assertTrue(mediaContentAvailable)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildChatMessage_sendMessage_receiveChatMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val successLatch = CountDownLatch(1)
        val executeScope = launch(UnconfinedTestDispatcher()) {
            runCatching {
                SendChatMessageUseCase(buildChatOutgoingMessage()).execute()
            }.onSuccess { message ->
                successLatch.countDown()
            }.onFailure { error ->
                fail("expected: NoException, actual: Exception, details: $error")
            }
        }
        successLatch.await(5, TimeUnit.MILLISECONDS)
        assertEquals(0, successLatch.count)

        executeScope.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildChatMessageWithEmptyDialogId_sendMessage_receiveError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val message = buildChatOutgoingMessage()
        message.setDialogId("")

        val executeScope = launch(UnconfinedTestDispatcher()) {
            runCatching {
                SendChatMessageUseCase(message).execute()
            }.onSuccess {
                fail("expected: Exception, actual: NoException")
            }
        }

        executeScope.cancel()
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun sendMessageToRemoteThrowException_sendChatMessage_receiveError() = runTest {
        val messagesRepository = object : MessagesRepositorySpy() {
            override fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity) {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        val executeScope = launch(UnconfinedTestDispatcher()) {
            runCatching {
                SendChatMessageUseCase(buildChatOutgoingMessage()).execute()
            }.onSuccess {
                fail("expected: Exception, actual: NoException")
            }
        }

        executeScope.cancel()
    }

    private fun buildChatOutgoingMessage(): OutgoingChatMessageEntity {
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val message = OutgoingChatMessageEntityImpl(null, contentType)
        message.setContent(System.currentTimeMillis().toString())
        message.setDialogId("test_dialog_id")
        message.setParticipantId(111)

        return message
    }
}