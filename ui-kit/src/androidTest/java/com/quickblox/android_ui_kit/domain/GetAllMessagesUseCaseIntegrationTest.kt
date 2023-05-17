/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.*
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetAllMessagesUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.users.model.QBUser
import junit.framework.Assert.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Constructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GetAllMessagesUseCaseIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var opponentChatService: QBChatService? = null

    private val loadedMessages = mutableListOf<MessageEntity?>()
    private var loadedPagination: PaginationEntity? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()

        createPrivateDialog()

        opponentChatService = createOpponentChatServiceAndLogin()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    private suspend fun createPrivateDialog() {
        runCatching {
            CreatePrivateDialogUseCase(USER_OPPONENT_ID_1).execute()
        }.onSuccess { result ->
            createdDialog = result
        }.onFailure { error ->
            fail("expected: Exception, actual: NotException")
        }
    }

    @After
    fun release() {
        deleteDialog(createdDialog)
        createdDialog = null

        loadedMessages.clear()
        loadedPagination = null

        opponentChatService?.destroy()

        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun sent10Messages_load1Page_loaded10Messages() = runBlocking {
        val chatMessagesCount = 5
        val eventMessagesCount = 5

        sendChatMessages(chatMessagesCount)
        sendEventMessages(eventMessagesCount)

        val job = loadMessagesByPage(1)

        val allSentMessagesCount = chatMessagesCount + eventMessagesCount
        assertEquals(allSentMessagesCount, loadedMessages.size)

        assertEquals(chatMessagesCount, getChatMessagesSizeFrom(loadedMessages))
        assertEquals(eventMessagesCount, getEventMessagesSizeFrom(loadedMessages))

        assertTrue(isAllIncomingChatMessagesContainsUser(loadedMessages))
        assertTrue(isAllChatMessagesContainsText(loadedMessages))
        assertTrue(isAllEventsContainsText(loadedMessages))
        assertTrue(isAllMessagesContainsTime(loadedMessages))

        assertEquals(10, loadedPagination!!.getPerPage())
        assertEquals(1, loadedPagination!!.getCurrentPage())
        assertTrue(loadedPagination!!.hasNextPage())

        job.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun sent20Messages_load2Pages_loaded20Messages() = runBlocking {
        val chatMessagesCount = 10
        val eventMessagesCount = 10

        sendChatMessages(chatMessagesCount)
        sendEventMessages(eventMessagesCount)

        val page1Job = loadMessagesByPage(1)
        val page2Job = loadMessagesByPage(2)

        val allSentMessagesCount = chatMessagesCount + eventMessagesCount
        assertEquals(allSentMessagesCount, loadedMessages.size)

        assertEquals(chatMessagesCount, getChatMessagesSizeFrom(loadedMessages))
        assertEquals(eventMessagesCount, getEventMessagesSizeFrom(loadedMessages))

        assertTrue(isAllIncomingChatMessagesContainsUser(loadedMessages))
        assertTrue(isAllChatMessagesContainsText(loadedMessages))
        assertTrue(isAllEventsContainsText(loadedMessages))
        assertTrue(isAllMessagesContainsTime(loadedMessages))

        assertEquals(10, loadedPagination!!.getPerPage())
        assertEquals(2, loadedPagination!!.getCurrentPage())
        assertTrue(loadedPagination!!.hasNextPage())

        page1Job.cancel()
        page2Job.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun sent20Messages_load3Pages_loaded20Messages() = runBlocking {
        val chatMessagesCount = 10
        val eventMessagesCount = 10

        sendChatMessages(chatMessagesCount)
        sendEventMessages(eventMessagesCount)

        val page1Job = loadMessagesByPage(1)
        val page2Job = loadMessagesByPage(2)
        val page3Job = loadMessagesByPage(3)

        val allSentMessagesCount = chatMessagesCount + eventMessagesCount
        assertEquals(allSentMessagesCount, loadedMessages.size)

        assertEquals(chatMessagesCount, getChatMessagesSizeFrom(loadedMessages))
        assertEquals(eventMessagesCount, getEventMessagesSizeFrom(loadedMessages))

        assertTrue(isAllIncomingChatMessagesContainsUser(loadedMessages))
        assertTrue(isAllChatMessagesContainsText(loadedMessages))
        assertTrue(isAllEventsContainsText(loadedMessages))
        assertTrue(isAllMessagesContainsTime(loadedMessages))

        assertEquals(10, loadedPagination!!.getPerPage())
        assertEquals(3, loadedPagination!!.getCurrentPage())
        assertFalse(loadedPagination!!.hasNextPage())

        page1Job.cancel()
        page2Job.cancel()
        page3Job.cancel()
    }

    private fun createOpponentChatServiceAndLogin(): QBChatService {
        val constructor: Constructor<QBChatService> = QBChatService::class.java.getDeclaredConstructor()
        constructor.isAccessible = true

        val qbChatService = constructor.newInstance()

        val opponentUser = QBUser()
        opponentUser.login = OPPONENT_LOGIN
        opponentUser.id = OPPONENT_ID
        opponentUser.password = OPPONENT_PASSWORD

        try {
            qbChatService.login(opponentUser)
        } catch (exception: Exception) {
            println("exception: ${exception.message}")
        }

        return qbChatService
    }

    private suspend fun loadMessagesByPage(page: Int): Job {
        val pageLatch = CountDownLatch(1)

        val job = loadMessagesByPage(page, pageLatch)
        pageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, pageLatch.count)

        return job
    }

    private suspend fun loadMessagesByPage(page: Int, completeLatch: CountDownLatch): Job {
        val pagination = PaginationEntityImpl()
        pagination.setPerPage(10)
        pagination.setCurrentPage(page)

        return CoroutineScope(Dispatchers.IO).launch {
            GetAllMessagesUseCase(createdDialog!!, pagination).execute().catch { error ->
                fail("expected: onCompletion, actual: catch, details: $error")
            }.onCompletion {
                completeLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    val message = result.getOrThrow().first
                    message?.let {
                        loadedMessages.add(message)
                    }

                    val receivedPagination = result.getOrThrow().second
                    loadedPagination = receivedPagination
                }
                if (result.isFailure) {
                    fail("expected: Result.Success, actual: Result.Failure")
                }
            }
        }
    }

    private fun sendEventMessages(messagesCount: Int) {
        sendMessages(messagesCount, true)
    }

    private fun sendChatMessages(messagesCount: Int) {
        sendMessages(messagesCount, false)
    }

    private fun sendMessages(messagesCount: Int, isEvent: Boolean) {
        for (index in 1..messagesCount) {
            val isNeedToSendFromOpponent = index % 5 == 0

            val qbChatService = if (isNeedToSendFromOpponent) {
                opponentChatService!!
            } else {
                QBChatService.getInstance()
            }

            sendMessageToDialog(isEvent, qbChatService, createdDialog!!)
        }
    }

    private fun sendMessageToDialog(isEvent: Boolean, chatService: QBChatService, dialog: DialogEntity) {
        val qbDialog = QBChatDialog()
        qbDialog.dialogId = dialog.getDialogId()
        qbDialog.setOccupantsIds(dialog.getParticipantIds()?.toList())
        qbDialog.type = QBDialogType.PRIVATE

        qbDialog.initForChat(chatService)

        val qbMessage = QBChatMessage()
        qbMessage.setSaveToHistory(true)

        if (isEvent) {
            qbMessage.setProperty("notification_type", "3")
            qbMessage.body = "group_dialog -> left users: TestUser3, TestUser4"
        } else {
            qbMessage.body = "test_message: ${System.currentTimeMillis()}"
        }

        qbDialog.sendMessage(qbMessage)
    }

    private fun getEventMessagesSizeFrom(messages: MutableList<MessageEntity?>): Int {
        var computedSize = 0

        messages.forEach { message ->
            val isEvent = message is EventMessageEntity
            if (isEvent) {
                ++computedSize
            }
        }

        return computedSize
    }

    private fun getChatMessagesSizeFrom(messages: MutableList<MessageEntity?>): Int {
        var computedSize = 0

        messages.forEach { message ->
            val isChatMessage = message is ChatMessageEntity
            if (isChatMessage) {
                ++computedSize
            }
        }

        return computedSize
    }

    private fun isAllIncomingChatMessagesContainsUser(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        val incomingChatMessages = messages.takeWhile { message ->
            message is IncomingChatMessageEntity
        }

        run breakLoop@{
            incomingChatMessages.forEach { message ->
                message as IncomingChatMessageEntity
                if (isIncomingChatMessageNotContainsUser(message)) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isIncomingChatMessageNotContainsUser(message: IncomingChatMessageEntity): Boolean {
        return message.getSender() == null
    }

    private fun isAllChatMessagesContainsText(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        val chatMessages = messages.takeWhile { message ->
            message is ChatMessageEntity
        }

        run breakLoop@{
            chatMessages.forEach { message ->
                message as ChatMessageEntity
                if (isChatMessageNotContainsText(message)) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isChatMessageNotContainsText(message: ChatMessageEntity): Boolean {
        return message.getContent() == null || message.getContent().isNullOrEmpty()
    }

    private fun isAllEventsContainsText(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        val events = messages.takeWhile { message ->
            message is EventMessageEntity
        }

        run breakLoop@{
            events.forEach { message ->
                message as EventMessageEntity
                if (isEventNotContainsText(message)) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isEventNotContainsText(message: EventMessageEntity): Boolean {
        return message.getText() == null || message.getText().isNullOrEmpty()
    }

    private fun isAllMessagesContainsTime(messages: MutableList<MessageEntity?>): Boolean {
        var contains = true

        run breakLoop@{
            messages.forEach { message ->
                if (isMessageNotContainsTime(message)) {
                    contains = false
                    return@breakLoop
                }
            }
        }

        return contains
    }

    private fun isMessageNotContainsTime(message: MessageEntity?): Boolean {
        return message != null && message.getTime() == null
    }
}