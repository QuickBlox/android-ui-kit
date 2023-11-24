/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.OPPONENT_ID
import com.quickblox.android_ui_kit.OPPONENT_PASSWORD
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetAllMessagesUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendChatMessageUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.*
import org.junit.runner.RunWith
import java.lang.reflect.Constructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SendChatMessageWithContentUseCaseIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var opponentChatService: QBChatService? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()

        opponentChatService = createOpponentChatServiceAndLogin()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    private fun createOpponentChatServiceAndLogin(): QBChatService {
        val constructor: Constructor<QBChatService> = QBChatService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true

        val qbChatService = constructor.newInstance()

        val opponentUser = QBUser()
        opponentUser.id = OPPONENT_ID
        opponentUser.password = OPPONENT_PASSWORD

        try {
            qbChatService.login(opponentUser)
        } catch (exception: Exception) {
            println("exception: ${exception.message}")
        }

        return qbChatService
    }

    @After
    fun release() = runBlocking {
        opponentChatService?.destroy()

        deleteDialog(createdDialog)
        createdDialog = null

        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessage_sendChatMessageToPrivateDialogAndGetAllMessages_messageReceived() = runBlocking {
        createdDialog = CreatePrivateDialogUseCase(OPPONENT_ID).execute()

        deleteAllMessagesIn(createdDialog)

        val createdMessage = buildOutgoingChatMessageWithMediaContent(createdDialog?.getDialogId()!!)

        val receiveMessageLatch = CountDownLatch(1)
        subscribeToReceiveDialogChatMessage(createdMessage.getMediaContent()!!, createdDialog!!, receiveMessageLatch)

        SendChatMessageUseCase(createdMessage).execute()

        receiveMessageLatch.await(10, TimeUnit.SECONDS)

        val loadMessagesLatch = CountDownLatch(1)
        var lastLoadedMessageFromRest: OutgoingChatMessageEntity? = null
        withContext(Dispatchers.IO) {
            GetAllMessagesUseCase(createdDialog!!, PaginationEntityImpl()).execute()
                .onCompletion {
                    loadMessagesLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    result.getOrThrow().first?.let { message ->
                        if (message is OutgoingChatMessageEntity) {
                            lastLoadedMessageFromRest = message
                        }
                    }
                }
        }

        loadMessagesLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, loadMessagesLatch.count)

        val receivedContentUrl = lastLoadedMessageFromRest?.getMediaContent()?.getUrl()
        assertTrue(receivedContentUrl!!.contains("https://api.quickblox.com/blobs"))

        val receivedMimeType = lastLoadedMessageFromRest?.getMediaContent()?.getMimeType()
        val createdMimeType = createdMessage.getMediaContent()?.getMimeType()
        assertEquals(createdMimeType, receivedMimeType)

        val receivedContentType = lastLoadedMessageFromRest?.getMediaContent()?.getType()
        val createdContentType = createdMessage.getMediaContent()?.getType()
        assertEquals(createdContentType!!.value, receivedContentType!!.value)
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun createChatMessage_sendChatMessageToGroupDialogAndGetAllMessages_messageReceived() = runBlocking {
        createdDialog = createGroupDialog()

        deleteAllMessagesIn(createdDialog)

        val createdMessage = buildOutgoingChatMessageWithMediaContent(createdDialog?.getDialogId()!!)

        val receiveMessageLatch = CountDownLatch(1)
        subscribeToReceiveDialogChatMessage(createdMessage.getMediaContent()!!, createdDialog!!, receiveMessageLatch)

        SendChatMessageUseCase(createdMessage).execute()

        receiveMessageLatch.await(10, TimeUnit.SECONDS)

        val loadMessagesLatch = CountDownLatch(1)
        var lastLoadedMessageFromRest: OutgoingChatMessageEntity? = null
        withContext(Dispatchers.IO) {
            GetAllMessagesUseCase(createdDialog!!, PaginationEntityImpl()).execute()
                .onCompletion {
                    loadMessagesLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    result.getOrThrow().first?.let { message ->
                        if (message is OutgoingChatMessageEntity) {
                            lastLoadedMessageFromRest = message
                        }
                    }
                }
        }

        loadMessagesLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, loadMessagesLatch.count)

        val receivedContentUrl = lastLoadedMessageFromRest?.getMediaContent()?.getUrl()
        val createdContentUrl = createdMessage.getMediaContent()?.getUrl()
        assertEquals(createdContentUrl, receivedContentUrl)

        val receivedMimeType = lastLoadedMessageFromRest?.getMediaContent()?.getMimeType()
        val createdMimeType = createdMessage.getMediaContent()?.getMimeType()
        assertEquals(createdMimeType, receivedMimeType)

        val receivedContentType = lastLoadedMessageFromRest?.getMediaContent()?.getType()
        val createdContentType = createdMessage.getMediaContent()?.getType()
        assertEquals(createdContentType!!.value, receivedContentType!!.value)
    }

    private suspend fun createGroupDialog(): DialogEntity? {
        val chatName = "test_group_chat: ${System.currentTimeMillis()}"
        val opponents = arrayListOf(OPPONENT_ID)
        return CreateGroupDialogUseCase(chatName, opponents).execute()
    }

    private fun deleteAllMessagesIn(dialog: DialogEntity?) {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog?.getDialogId()!!

        val loadedMessages = QBRestChatService.getDialogMessages(qbDialog, null).perform()

        val needToDeleteMessageIds = arrayListOf<String>()
        loadedMessages.forEach { qbChatMessage ->
            needToDeleteMessageIds.add(qbChatMessage.id)
        }

        if (needToDeleteMessageIds.isNotEmpty()) {
            QBRestChatService.deleteMessages(needToDeleteMessageIds.toSet(), true).perform()
        }
    }

    private fun buildOutgoingChatMessageWithMediaContent(dialogId: String): OutgoingChatMessageEntity {
        val type = ChatMessageEntity.ContentTypes.MEDIA

        val message = OutgoingChatMessageEntityImpl(null, type)
        message.setDialogId(dialogId)
        message.setParticipantId(OPPONENT_ID)
        message.setTime(System.currentTimeMillis())
        message.setContent("test_message_with_media_content: ${System.currentTimeMillis()}")
        message.setMediaContent(buildMediaContent())

        return message
    }

    private fun buildMediaContent(): MediaContentEntity {
        return MediaContentEntityImpl("test_song", "https://test.com/test_song.mp3", "audio/mpeg")
    }

    private fun subscribeToReceiveDialogChatMessage(
        createdContent: MediaContentEntity,
        createdDialog: DialogEntity,
        receivedMessageLatch: CountDownLatch
    ) {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = createdDialog.getDialogId()!!
        qbDialog.type = getQBDialogTypeBy(createdDialog)

        val isPrivateDialog = qbDialog.type == QBDialogType.PRIVATE
        if (isPrivateDialog) {
            qbDialog.setOccupantsIds(arrayListOf(createdDialog.getOwnerId()))
        } else {
            qbDialog.join(null)
        }

        qbDialog.initForChat(opponentChatService)

        qbDialog.addMessageListener(object : QBChatDialogMessageListener {
            override fun processMessage(p0: String?, qbChatMessage: QBChatMessage?, p2: Int?) {
                val isNotFromOpponentChatUser = opponentChatService?.user?.id != qbChatMessage?.senderId
                if (isNotFromOpponentChatUser) {
                    val attachment = qbChatMessage?.attachments!!.toList()[0]

                    val loadedUrl = attachment?.url
                    val createdUrl = createdContent.getUrl()
                    assertEquals(createdUrl, loadedUrl)

                    val loadedContentType = attachment.contentType
                    val createdContentType = createdContent.getMimeType()
                    assertEquals(createdContentType, loadedContentType)

                    receivedMessageLatch.countDown()
                }
            }

            override fun processError(p0: String?, exception: QBChatException?, p2: QBChatMessage?, p3: Int?) {
                exception.toString()
            }
        })
    }

    private fun getQBDialogTypeBy(dialog: DialogEntity): QBDialogType {
        when (dialog.getType()) {
            DialogEntity.Types.GROUP -> {
                return QBDialogType.GROUP
            }
            DialogEntity.Types.PRIVATE -> {
                return QBDialogType.PRIVATE
            }
            else -> {
                throw IllegalArgumentException("Wrong parsing for dialog type: ${dialog.getType()}")
            }
        }
    }
}