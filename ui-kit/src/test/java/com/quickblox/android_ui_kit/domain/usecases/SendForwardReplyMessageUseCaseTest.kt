/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.repository.message.MessagesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
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
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

private const val SPY_DIALOG_ID = "spy_dialog_id"

class SendForwardReplyMessageUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun createMediaContentEntity_makeMessageBodyFromMediaContent_messageBodyEquals() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val mediaContentEntity = MediaContentEntityImpl("dummy_file_name", "dummy_file_url", "dummy_mime_type")
        val messageBody = buildUseCase().makeMessageBodyFromMediaContent(mediaContentEntity)
        assertEquals("MediaContentEntity|dummy_file_name|dummy_file_url|dummy_mime_type", messageBody)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun mediaContentEntityWithUidWithoutJson_getUidFom_correctUid() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val createdUid = "d5c1422f48f24f3593be0229e0cffd4800"

        val url =
            "https://api.quickblox.com/blobs/$createdUid?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdHlwZSI6ImFwcGxpY2F0aW9uIiwiYXBwbGljYXRpb25faWQiOjc1OTQ5LCJpYXQiOjE3MDA2NDYxMjAzOTY2ODl9.A8viDHB0_aAMfmI8n8_24WsxtIwM5yIuNcwng6a-xSyNzznqnATjt3O1TVs2WEfyD6yzM4uSyTgJGqLm03CpHg"
        val parsedUid = buildUseCase().getUidFom(url)
        assertEquals(createdUid, parsedUid)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun mediaContentEntityWithWrongUid_getUidFom_correctUid() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val url = "https://wrong_link/blobs"
        val parsedUid = buildUseCase().getUidFom(url)
        assertTrue(parsedUid!!.isEmpty())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun messageExistAndDialogExist_sendMessage_noErrors() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        buildUseCase().sendMessage(buildForwardMessageEntity(), DialogEntitySpy())
    }

    @Test(expected = MessagesRepositoryException::class)
    @ExperimentalCoroutinesApi
    fun sendMessageThrowException_sendMessage_receivedException() = runTest {
        val messagesRepository = object : MessagesRepositorySpy() {
            override fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity) {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.CONNECTION_FAILED, "error")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        buildUseCase().sendMessage(buildForwardMessageEntity(), DialogEntitySpy())
    }

    private fun buildUseCase(): SendForwardReplyMessageUseCase {
        return SendForwardReplyMessageUseCase(buildForwardMessageEntity(), SPY_DIALOG_ID)
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogIdIsEmpty_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        SendForwardReplyMessageUseCase(buildForwardMessageEntity(), "").execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogNotExistInCache_execute_receivedException() = runTest {
        val dialogsRepository = object : DialogsRepositorySpy() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, "error")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })

        SendForwardReplyMessageUseCase(buildForwardMessageEntity(), SPY_DIALOG_ID).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun forwardedMessageIsCorrectAndSendMessageThrowException_execute_receivedException() = runTest {
        val messagesRepository = object : MessagesRepositorySpy() {
            override fun sendChatMessageToRemote(entity: OutgoingChatMessageEntity, dialog: DialogEntity) {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.CONNECTION_FAILED, "error")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        SendForwardReplyMessageUseCase(buildForwardMessageEntity(), SPY_DIALOG_ID).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun forwardedTextMessageIsCorrect_execute_noErrors() = runTest {
        val remoteDataSource: RemoteDataSource = object : RemoteDataSourceImpl() {
            override fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
                assertTrue(messageDTO.isForwardedOrReplied!!)
                assertNotNull(messageDTO.properties)
            }

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

        val forwardMessage = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT).apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.FORWARDED)
            setForwardedRepliedMessages(buildTextForwardMessages())
            setDialogId("forward_message_dialog_id")
        }

        SendForwardReplyMessageUseCase(forwardMessage, SPY_DIALOG_ID).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun forwardedMediaMessageIsCorrect_execute_noErrors() = runTest {
        val remoteDataSource: RemoteDataSource = object : RemoteDataSourceImpl() {
            override fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
                assertTrue(messageDTO.isForwardedOrReplied!!)
                assertNotNull(messageDTO.properties)
            }

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

        val forwardMessage = OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT).apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.FORWARDED)
            setForwardedRepliedMessages(buildMediaForwardMessages())
            setDialogId("forward_message_dialog_id")
        }

        SendForwardReplyMessageUseCase(forwardMessage, SPY_DIALOG_ID).execute()
    }

    private fun buildForwardMessageEntity(): OutgoingChatMessageEntity {
        return OutgoingChatMessageEntityImpl(null, ChatMessageEntity.ContentTypes.TEXT).apply {
            setForwardOrReplied(ForwardedRepliedMessageEntity.Types.FORWARDED)
        }
    }

    private fun buildTextForwardMessages(count: Int = 5): List<IncomingChatMessageEntity> {
        val createdMessages = mutableListOf<IncomingChatMessageEntity>()

        for (index in 1..count) {
            val message = buildTextForwardMessage()
            createdMessages.add(message)
        }

        return createdMessages
    }

    private fun buildMediaForwardMessages(count: Int = 5): List<IncomingChatMessageEntity> {
        val createdMessages = mutableListOf<IncomingChatMessageEntity>()

        for (index in 1..count) {
            val message = buildMediaForwardMessage()
            createdMessages.add(message)
        }

        return createdMessages
    }

    private fun buildTextForwardMessage(): IncomingChatMessageEntity {
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

    private fun buildMediaForwardMessage(): IncomingChatMessageEntity {
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