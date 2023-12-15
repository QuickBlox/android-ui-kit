/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.message.MessagesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CreateReplyMessageUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessageEntityWithMediaContent_isMediaContentNotAvailableIn_receivedFalse() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = CreateReplyMessageUseCase(buildTextReplyMessage(), buildRelateMessage())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.MEDIA)
        message.setMediaContent(MediaContentEntityImpl("dummy_file_name", "dummy_file_url", "dummy_mime_type"))

        val mediaContentAvailable = useCase.isMediaContentNotAvailableIn(message)
        assertFalse(mediaContentAvailable)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessageEntityWithoutMediaContent_isMediaContentNotAvailableIn_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = CreateReplyMessageUseCase(buildTextReplyMessage(), buildRelateMessage())

        val message = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT)

        val mediaContentAvailable = useCase.isMediaContentNotAvailableIn(message)
        assertTrue(mediaContentAvailable)
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun replyMessagesNotHasDialogId_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val replyMessage = buildTextReplyMessage().apply {
            setDialogId("")
        }

        CreateReplyMessageUseCase(replyMessage, buildRelateMessage()).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun relateMessagesNotHasDialogId_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val relateMessage = buildRelateMessage().apply {
            setDialogId("")
        }

        CreateReplyMessageUseCase(buildTextReplyMessage(), relateMessage).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun messageNotReplied_markReplied_messageReplied() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = CreateReplyMessageUseCase(buildTextReplyMessage(), buildRelateMessage())
        val markedMessage = useCase.markAsReplied(buildRelateMessage())

        assertTrue(markedMessage.isReplied())
        assertTrue(markedMessage.isForwardedOrReplied())
        assertFalse(markedMessage.isForwarded())
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun createReplyMessageThrownException_execute_receivedException() = runTest {
        val messagesRepository: MessagesRepository = object : MessagesRepositorySpy() {
            override fun createReplyMessage(
                replyMessage: ForwardedRepliedMessageEntity, relateMessage: OutgoingChatMessageEntity
            ): OutgoingChatMessageEntity {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.UNEXPECTED, "")
            }
        }
        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        CreateReplyMessageUseCase(buildTextReplyMessage(), buildRelateMessage()).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogIdIsEmptyInReplyMessageAndRelateMessageNotExist_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val replyMessage = buildTextReplyMessage()
        replyMessage.setDialogId("")

        CreateReplyMessageUseCase(replyMessage, buildRelateMessage()).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun replyTextAndRelateMessageAreCorrect_execute_noErrors() = runTest {
        val remoteDataSource: RemoteDataSource = object : RemoteDataSourceImpl() {
            override fun getLoggedUserIdOrThrowException(): Int {
                return 1001
            }
        }

        val dependency: Dependency = object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return MessagesRepositoryImpl(remoteDataSource)
            }
        }
        QuickBloxUiKit.setDependency(dependency)

        val createdMessage = CreateReplyMessageUseCase(buildTextReplyMessage(), buildRelateMessage()).execute()

        assertFalse(createdMessage!!.isForwarded())
        assertEquals(1, createdMessage.getForwardedRepliedMessages()!!.count())
        assertFalse(createdMessage.getForwardedRepliedMessages()!![0].isMediaContent())
        assertTrue(createdMessage.isReplied())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun replyMediaAndRelateMessageAreCorrect_execute_noErrors() = runTest {
        val remoteDataSource: RemoteDataSource = object : RemoteDataSourceImpl() {
            override fun getLoggedUserIdOrThrowException(): Int {
                return 1001
            }
        }

        val dependency: Dependency = object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return MessagesRepositoryImpl(remoteDataSource)
            }
        }
        QuickBloxUiKit.setDependency(dependency)

        val createdMessage = CreateReplyMessageUseCase(buildMediaReplyMessage(), buildRelateMessage()).execute()

        assertFalse(createdMessage!!.isForwarded())
        assertEquals(1, createdMessage.getForwardedRepliedMessages()!!.count())
        assertTrue(createdMessage.getForwardedRepliedMessages()!![0].isMediaContent())
        assertTrue(createdMessage.isReplied())
    }

    private fun buildTextReplyMessage(): IncomingChatMessageEntity {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()
        val time = System.currentTimeMillis()
        val content = "temp_content_${System.currentTimeMillis()}"

        val message = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT)
        message.setParticipantId(participantId)
        message.setSenderId(senderId)
        message.setDialogId(dialogId)
        message.setMessageId(messageId)
        message.setTime(time)
        message.setContent(content)
        message.setSender(buildSender())

        return message
    }

    private fun buildMediaReplyMessage(): IncomingChatMessageEntity {
        val participantId = Random.nextInt(1000, 2000)
        val senderId = Random.nextInt(2000, 3000)
        val dialogId = System.currentTimeMillis().toString()
        val messageId = System.currentTimeMillis().toString()
        val time = System.currentTimeMillis()
        val content = "temp_content_${System.currentTimeMillis()}"

        val message = IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.MEDIA)
        message.setParticipantId(participantId)
        message.setSenderId(senderId)
        message.setDialogId(dialogId)
        message.setMessageId(messageId)
        message.setTime(time)
        message.setContent(content)
        message.setSender(buildSender())
        message.setMediaContent(buildMediaContent())

        return message
    }

    private fun buildMediaContent(): MediaContentEntity {
        val testValue = System.currentTimeMillis()
        val mediaContent = MediaContentEntityImpl(
            "test_file_name_$testValue", "test_file_url_$testValue", "test_file_mime_type_$testValue"
        )

        return mediaContent
    }

    private fun buildSender(): UserEntity {
        val user = UserEntityImpl()
        user.setName("test_sender_name_${System.currentTimeMillis()}")

        return user
    }

    private fun buildRelateMessage(): OutgoingChatMessageEntity {
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val message = OutgoingChatMessageEntityImpl(null, contentType)
        message.setContent(System.currentTimeMillis().toString())
        message.setDialogId("test_dialog_id")
        message.setParticipantId(111)
        message.setSender(buildSender())

        return message
    }
}