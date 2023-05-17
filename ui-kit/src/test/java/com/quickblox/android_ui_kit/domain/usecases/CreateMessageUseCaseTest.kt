/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import android.net.Uri
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CreateMessageUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun createMediaMessage_isTextMessage_receivedFalse() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isTextMessage = buildCreateMessageUseCase().isTextMessage(ChatMessageEntity.ContentTypes.MEDIA)
        assertFalse(isTextMessage)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createTextMessage_isTextMessage_receivedTrue() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isTextMessage = buildCreateMessageUseCase().isTextMessage(ChatMessageEntity.ContentTypes.TEXT)
        assertTrue(isTextMessage)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createTextMessage_isMediaMessage_receivedFalse() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isMediaMessage = buildCreateMessageUseCase().isMediaMessage(ChatMessageEntity.ContentTypes.TEXT)
        assertFalse(isMediaMessage)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createMediaMessage_isMediaMessage_receivedTrue() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isMediaMessage = buildCreateMessageUseCase().isMediaMessage(ChatMessageEntity.ContentTypes.MEDIA)
        assertTrue(isMediaMessage)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createCorrectFile_isIncorrectFile_receivedFalse() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = File("testPathName")
        file.writeBytes("testBytes".toByteArray())

        val fileEntity = FileEntityImpl()
        fileEntity.setFile(file)
        fileEntity.setUri(Uri.fromFile(file))
        fileEntity.setUrl("android://content-data/test.file")
        fileEntity.setMimeType("image/png")

        val isIncorrectFile = buildCreateMessageUseCase().isIncorrectFile(fileEntity)

        file.delete()

        assertFalse(isIncorrectFile)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createNullFile_isIncorrectFile_receivedTrue() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isIncorrectFile = buildCreateMessageUseCase().isIncorrectFile(null)
        assertTrue(isIncorrectFile)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createEmptyFile_isIncorrectFile_receivedTrue() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val fileEntity = FileEntityImpl()
        val isIncorrectFile = buildCreateMessageUseCase().isIncorrectFile(fileEntity)
        assertTrue(isIncorrectFile)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveAllParameters_createMediaMessage_receivedMediaMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = buildCreateMessageUseCase()

        val fileName = "a"
        val fileUrl = "https://test.com/a.mp3"
        val mimeType = "audio/mpeg"
        val mediaContent = useCase.createMediaContent(fileName, fileUrl, mimeType)

        val dialogId = "dialogId"
        val mediaMessage = useCase.createMediaMessage(dialogId, mediaContent)

        assertEquals(dialogId, mediaMessage.getDialogId())
        assertEquals(fileUrl, mediaMessage.getMediaContent()!!.getUrl())
        assertEquals(mimeType, mediaMessage.getMediaContent()!!.getMimeType())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveAllParameters_createMediaMessage_receivedMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialogId = "dialogId"
        val contentType = ChatMessageEntity.ContentTypes.MEDIA
        val content = "temp_content"
        val mediaMessage = buildCreateMessageUseCase().createMessage(dialogId, content, contentType)
        assertEquals(dialogId, mediaMessage.getDialogId())
        assertEquals(contentType, mediaMessage.getContentType())
        assertEquals(content, mediaMessage.getContent())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveAllParameters_createTextMessage_receivedMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialogId = "dialogId"
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val content = "temp_content"
        val mediaMessage = buildCreateMessageUseCase().createMessage(dialogId, content, contentType)
        assertEquals(dialogId, mediaMessage.getDialogId())
        assertEquals(contentType, mediaMessage.getContentType())
        assertEquals(content, mediaMessage.getContent())
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun textMessageAndContentIsEmpty_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        buildCreateMessageUseCase(contentType = ChatMessageEntity.ContentTypes.TEXT, content = "").execute()
        fail("expected: Exception, actual: NotException")
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun mediaMessageAndNullFile_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        buildCreateMessageUseCase(contentType = ChatMessageEntity.ContentTypes.MEDIA, fileEntity = null).execute()
        fail("expected: Exception, actual: NotException")
    }

    private fun buildCreateMessageUseCase(
        contentType: ChatMessageEntity.ContentTypes = ChatMessageEntity.ContentTypes.TEXT,
        content: String = "testContent",
        fileEntity: FileEntity? = null
    ): CreateMessageUseCase {
        return CreateMessageUseCase(contentType, "testDialogId", content, fileEntity)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveTextTypeParameter_execute_receivedTextMessage() = runTest {
        val participantId = 888
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val content = "testContent"
        val dialogId = "temp_dialog_id"

        val messagesRepository = object : MessagesRepositorySpy() {
            override fun createMessage(entity: OutgoingChatMessageEntity): OutgoingChatMessageEntity {
                val message = OutgoingChatMessageEntityImpl(null, contentType)
                message.setParticipantId(participantId)
                message.setContent(content)
                message.setDialogId(dialogId)

                return message
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        val receivedMessageLatch = CountDownLatch(1)

        CreateMessageUseCase(contentType, dialogId, content).execute().collect { createdMessage ->
            assertEquals(participantId, createdMessage?.getParticipantId())
            assertEquals(contentType, createdMessage?.getContentType())
            assertEquals(dialogId, createdMessage?.getDialogId())
            assertEquals(content, createdMessage?.getContent())

            receivedMessageLatch.countDown()
        }

        receivedMessageLatch.await(3, TimeUnit.SECONDS)
        assertEquals(0, receivedMessageLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveMediaTypeParameter_execute_receivedMediaMessage() = runTest {
        val participantId = 888
        val contentType = ChatMessageEntity.ContentTypes.MEDIA
        val content = "temp_content"
        val dialogId = "temp_dialog_id"

        val messagesRepository = object : MessagesRepositorySpy() {
            override fun createMessage(entity: OutgoingChatMessageEntity): OutgoingChatMessageEntity {
                val message = OutgoingChatMessageEntityImpl(null, contentType)
                message.setParticipantId(participantId)
                message.setContent(content)
                message.setDialogId(dialogId)

                return message
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        val file = File("testPathName")
        file.writeBytes("testBytes".toByteArray())

        val fileEntity = FileEntityImpl()
        fileEntity.setFile(file)
        fileEntity.setUri(Uri.fromFile(file))
        fileEntity.setUrl("android://content-data/test.file")
        fileEntity.setMimeType("image/png")

        val receivedMessageLatch = CountDownLatch(2)

        CreateMessageUseCase(contentType, dialogId, content, fileEntity).execute().collect { createdMessage ->
            assertEquals(participantId, createdMessage?.getParticipantId())
            assertEquals(contentType, createdMessage?.getContentType())
            assertEquals(dialogId, createdMessage?.getDialogId())
            assertEquals(content, createdMessage?.getContent())

            receivedMessageLatch.countDown()
        }

        receivedMessageLatch.await(3, TimeUnit.SECONDS)

        file.delete()

        assertEquals(0, receivedMessageLatch.count)
    }
}