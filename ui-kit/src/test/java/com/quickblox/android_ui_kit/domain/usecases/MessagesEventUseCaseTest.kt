/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.repository.mapper.MessageMapper
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.entity.UserEntitySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
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
    fun senderIdExistInUsers_getUser_userExist() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val userA = UserEntitySpy()
        userA.setUserId(888)

        val userB = UserEntitySpy()
        userB.setUserId(777)

        val user = MessagesEventUseCase(DialogEntitySpy()).getUser(888, arrayListOf(userA, userB))
        assertEquals(888, user!!.getUserId())
    }

    @Test
    fun senderIdIsNull_getUser_userExist() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val userA = UserEntitySpy()
        userA.setUserId(888)

        val userB = UserEntitySpy()
        userB.setUserId(777)

        val user = MessagesEventUseCase(DialogEntitySpy()).getUser(null, arrayListOf(userA, userB))
        assertNull(user)
    }

    @Test
    fun senderIdNotExistInUsersButExistInRemote_getUser_userExist() {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                val remoteUser = UserEntitySpy()
                remoteUser.setUserId(888)

                return remoteUser
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val userA = UserEntitySpy()
        userA.setUserId(777)

        val userB = UserEntitySpy()
        userB.setUserId(555)

        val user = MessagesEventUseCase(DialogEntitySpy()).getUser(888, arrayListOf(userA, userB))
        assertEquals(888, user!!.getUserId())
    }

    @Test
    fun createDialog_getUsersFrom_received2Users() {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                if (userId == 777) {
                    val userA = UserEntitySpy()
                    userA.setUserId(777)

                    return userA
                }

                if (userId == 555) {
                    val userB = UserEntitySpy()
                    userB.setUserId(555)

                    return userB
                }

                throw UsersRepositoryException(UsersRepositoryException.Codes.NOT_FOUND_ITEM, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val dialog = object : DialogEntitySpy() {
            override fun getParticipantIds(): Collection<Int> {
                return arrayListOf(555, 777)
            }
        }

        val users = MessagesEventUseCase(DialogEntitySpy()).getUsersFrom(dialog)

        assertEquals(2, users.size)
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogIdIsEmpty_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = object : DialogEntitySpy() {
            override fun getDialogId(): String {
                return ""
            }
        }

        MessagesEventUseCase(dialog).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogIdIsNull_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = object : DialogEntitySpy() {
            override fun getDialogId(): String? {
                return null
            }
        }

        MessagesEventUseCase(dialog).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun participantsAreEmpty_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = object : DialogEntitySpy() {
            override fun getParticipantIds(): Collection<Int> {
                return arrayListOf()
            }
        }

        MessagesEventUseCase(dialog).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun participantsAreNull_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = object : DialogEntitySpy() {
            override fun getParticipantIds(): Collection<Int>? {
                return null
            }
        }

        MessagesEventUseCase(dialog).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun messageEntityIsNull_addUserToMessage_receivedNull() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = object : DialogEntitySpy() {
            override fun getParticipantIds(): Collection<Int>? {
                return null
            }
        }

        val userA = UserEntitySpy()
        userA.setUserId(777)

        val userB = UserEntitySpy()
        userB.setUserId(555)

        val usersFromDialog = arrayListOf(userA, userB)

        val message = MessagesEventUseCase(dialog).addUserToMessage(null, usersFromDialog)
        assertNull(message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun messageEntityExist_addUserToMessage_receivedMessageEntity() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val messageEntity = object : IncomingChatMessageEntityImpl(ChatMessageEntity.ContentTypes.TEXT) {
            override fun getSenderId(): Int {
                return 777
            }
        }

        val userA = UserEntitySpy()
        userA.setUserId(777)

        val userB = UserEntitySpy()
        userB.setUserId(555)

        val usersFromDialog = arrayListOf(userA, userB)

        val message = MessagesEventUseCase(DialogEntitySpy()).addUserToMessage(messageEntity, usersFromDialog)
        assertEquals(777, message?.getSender()?.getUserId()!!)
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