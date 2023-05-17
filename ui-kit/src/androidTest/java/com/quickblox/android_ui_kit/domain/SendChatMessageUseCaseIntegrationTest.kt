/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.USER_OPPONENT_ID_1
import com.quickblox.android_ui_kit.USER_OPPONENT_ID_2
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SendChatMessageUseCaseIntegrationTest : BaseTest() {
    private var createdDialog: DialogEntity? = null
    private var connectionUseCase: ConnectionUseCase? = null

    @Before
    fun init() = runBlocking {
        initDependency()
        initQuickblox()
        loginToRest()

        initConnectionUseCase()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    private suspend fun initConnectionUseCase() {
        if (connectionUseCase == null) {
            connectionUseCase = ConnectionUseCase()
        }
        connectionUseCase?.execute()
    }

    @After
    fun release() = runBlocking {
        deleteDialog(createdDialog)
        createdDialog = null

        connectionUseCase?.release()

        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessage_sendChatMessageToPrivateDialogAndGetAllMessages_messageReceived() = runBlocking {
        createPrivateDialog()

        val sendMessageLatch = CountDownLatch(1)

        val createdMessage = buildOutgoingChatMessage()

        withContext(Dispatchers.IO) {
            runCatching {
                SendChatMessageUseCase(createdMessage).execute()
            }.onSuccess { result ->
                sendMessageLatch.countDown()
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        sendMessageLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, sendMessageLatch.count)

        val loadMessagesLatch = CountDownLatch(1)
        var lastLoadedMessageFromRest: OutgoingChatMessageEntity? = null
        withContext(Dispatchers.IO) {
            GetAllMessagesUseCase(createdDialog!!, PaginationEntityImpl()).execute()
                .onCompletion {
                    loadMessagesLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    val message = result.getOrThrow().first
                    if (message is OutgoingChatMessageEntity) {
                        lastLoadedMessageFromRest = message
                    }
                }
        }

        loadMessagesLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, loadMessagesLatch.count)

        assertEquals(createdMessage.getContent(), lastLoadedMessageFromRest!!.getContent())
    }

    private suspend fun createPrivateDialog() {
        runCatching {
            CreatePrivateDialogUseCase(USER_OPPONENT_ID_1).execute()
        }.onSuccess { result ->
            createdDialog = result
        }.onFailure { error ->
            fail("expected: NotException, actual: Exception, details $error")
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createChatMessage_sendChatMessageToGroupDialogAndGetAllMessages_messageReceived() = runBlocking {
        createGroupDialog()

        val sendMessageLatch = CountDownLatch(1)

        val createdMessage = buildOutgoingChatMessage()

        withContext(Dispatchers.IO) {
            runCatching {
                SendChatMessageUseCase(createdMessage).execute()
            }.onSuccess { result ->
                sendMessageLatch.countDown()
            }.onFailure { error ->
                fail("expected: NotException, actual: Exception, details $error")
            }
        }

        sendMessageLatch.await(10, TimeUnit.SECONDS)
        assertEquals(0, sendMessageLatch.count)

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

        assertEquals(createdMessage.getContent(), lastLoadedMessageFromRest!!.getContent())
    }

    private suspend fun createGroupDialog() {
        runCatching {
            val chatName = "test_group_chat: ${System.currentTimeMillis()}"
            val opponents = arrayListOf(USER_OPPONENT_ID_1, USER_OPPONENT_ID_2)
            CreateGroupDialogUseCase(chatName, opponents).execute()
        }.onSuccess { result ->
            createdDialog = result
        }.onFailure { error ->
            fail("expected: NotException, actual: Exception, details $error")
        }
    }

    private fun buildOutgoingChatMessage(): OutgoingChatMessageEntity {
        val type = ChatMessageEntity.ContentTypes.TEXT

        val message = OutgoingChatMessageEntityImpl(null, type)
        message.setDialogId(createdDialog!!.getDialogId())
        message.setParticipantId(getParticipantId())
        message.setContent("test_message: ${System.currentTimeMillis()}")
        message.setTime(System.currentTimeMillis())

        return message
    }

    private fun getParticipantId(): Int {
        val participantIdsList = createdDialog?.getParticipantIds()?.toMutableList()

        val loggedUserId = QuickBloxUiKit.getDependency().getUsersRepository().getLoggedUserId()
        participantIdsList?.remove(loggedUserId)

        val isGroupDialog = createdDialog?.getType() == DialogEntity.Types.GROUP
        if (isGroupDialog) {
            val needToDeleteOpponentId = participantIdsList?.get(0)
            participantIdsList?.remove(needToDeleteOpponentId)
        }

        if (participantIdsList?.size != 1) {
            throw RuntimeException("The participants count for private dialog has wrong value")
        }

        return participantIdsList.toList()[0]
    }
}