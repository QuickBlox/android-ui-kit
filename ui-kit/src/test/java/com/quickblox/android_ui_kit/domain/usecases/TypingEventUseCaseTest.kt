/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
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

class TypingEventUseCaseTest : BaseTest() {
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
    @ExperimentalCoroutinesApi
    fun typingNamesHave3Users_parseAllTypingName_received3TypingNames() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val typingNames = "Bob|Mark|John"
        val actualTypingNames = useCase.makeTypingTextFrom(typingNames)

        assertEquals("Bob and 2 others are typing...", actualTypingNames)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun typingNamesHave1User_parseAllTypingName_received1TypingNames() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val typingNames = "Bob"
        val actualTypingNames = useCase.makeTypingTextFrom(typingNames)

        assertEquals("Bob typing...", actualTypingNames)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun typingNamesEmpty_parseAllTypingName_receivedEmptyTypingNames() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val actualTypingNames = useCase.makeTypingTextFrom("")

        assertTrue(actualTypingNames.isEmpty())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildUserWithName_getUserNameFrom_receivedUserName() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val user = buildUserEntity(888)
        val generatedUserName = System.currentTimeMillis().toString()
        user.setName(generatedUserName)

        val receivedUserName = useCase.getUserNameFrom(user)

        assertEquals(generatedUserName, receivedUserName)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildUserWithLogin_getUserNameFrom_receivedUserName() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val user = buildUserEntity(888)
        val generatedLogin = System.currentTimeMillis().toString()
        user.setLogin(generatedLogin)

        val receivedUserName = useCase.getUserNameFrom(user)

        assertEquals(generatedLogin, receivedUserName)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildUserWithoutLoginAndName_getUserNameFrom_receivedUserWithoutName() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserName = useCase.getUserNameFrom(buildUserEntity(888))

        assertEquals("User without name", receivedUserName)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have0Name_parseTypingNamesFrom_received0Name() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("")

        assertEquals(0, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have1Name_parseTypingNamesFrom_received1Name() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("Bob")

        assertEquals(1, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have1NameWithSpaces_parseTypingNamesFrom_received1Name() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("  Bob   ")

        assertEquals(1, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have2NamesWithSpaces_parseTypingNamesFrom_received2Names() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("  Bob   | Michael        ")

        assertEquals(2, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have1NameWithSpacesAndSlashes_parseTypingNamesFrom_received1Name() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("  Bob   |        |||")

        assertEquals(1, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have2NamesWithSpacesAndSlashes_parseTypingNamesFrom_received2Names() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val receivedUserNames = useCase.parseTypingNamesFrom("  Bob   | Michael        |||")

        assertEquals(2, receivedUserNames.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun startTimer_wait5Seconds_timerStopped() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())
        useCase.timerLength = 3000

        useCase.startTypingTimer(buildUserEntity(888), useCase.typingTimers)

        assertEquals(1, useCase.typingTimers.size)

        CountDownLatch(1).await(5, TimeUnit.SECONDS)

        assertEquals(0, useCase.typingTimers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun startTimer_wait1SecondAndStop_timerStopped() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val user = buildUserEntity(888)

        useCase.startTypingTimer(user, useCase.typingTimers)

        assertEquals(1, useCase.typingTimers.size)

        CountDownLatch(1).await(1, TimeUnit.SECONDS)

        useCase.stopTypingTimer(user, useCase.typingTimers)

        assertEquals(0, useCase.typingTimers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun start3Timers_getAllTypingNames_checkTimerStopped() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        useCase.startTypingTimer(buildUserEntity(555), useCase.typingTimers)
        useCase.startTypingTimer(buildUserEntity(777), useCase.typingTimers)
        useCase.startTypingTimer(buildUserEntity(888), useCase.typingTimers)

        val allTypingNames = useCase.getAllTypingNamesFrom(useCase.typingTimers)

        CountDownLatch(1).await(4, TimeUnit.SECONDS)

        val names = useCase.parseTypingNamesFrom(allTypingNames)

        assertEquals(3, names.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun exist3UsersAndSenderIdExist_getUserBySenderId_receivedUser() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val userA = buildUserEntity(888)
        val userB = buildUserEntity(777)
        val userC = buildUserEntity(555)

        val foundUser = useCase.getUserBySenderId(888, arrayListOf(userA, userB, userC))

        assertNotNull(foundUser)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun exist3UsersAndSenderIdNotExist_getUserBySenderId_receivedUser() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val userA = buildUserEntity(888)
        val userB = buildUserEntity(777)
        val userC = buildUserEntity(555)

        val foundUser = useCase.getUserBySenderId(333, arrayListOf(userA, userB, userC))

        assertNull(foundUser)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun emptyUsersAndSenderId_getUserBySenderId_receivedUser() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = TypingEventUseCase(buildDialogEntity())

        val foundUser = useCase.getUserBySenderId(333, arrayListOf())

        assertNull(foundUser)
    }

    private fun buildUserEntity(userId: Int): UserEntity {
        val userEntity = UserEntityImpl()
        userEntity.setUserId(userId)

        return userEntity
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogEntityWithoutParticipants_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialog = buildDialogEntity()
        dialog.setParticipantIds(mutableListOf())

        TypingEventUseCase(dialog).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    @Ignore
    fun executeAndStartTyping3Times_wait7Seconds_receivedTyping() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity> {
                val users = mutableListOf<UserEntity>()

                val userA = buildUserEntity(555)
                userA.setName("Bob")
                users.add(userA)

                val userB = buildUserEntity(777)
                userB.setLogin("Michael")
                users.add(userB)

                val userC = buildUserEntity(888)
                userC.setName("John")
                users.add(userC)

                return users
            }
        }

        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }

            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val receivedTypingLatch = CountDownLatch(3)

        var actualTypingNames = ""

        val executeScope = launch(UnconfinedTestDispatcher()) {
            val useCase = TypingEventUseCase(buildDialogEntity())
            useCase.timerLength = 5000
            useCase.execute().collect { typingEntity ->
                val receivedTypingNames = typingEntity!!.getText()
                actualTypingNames = receivedTypingNames
                receivedTypingLatch.countDown()
            }
        }

        eventsRepository.sendStartTyping(555)
        eventsRepository.sendStartTyping(777)
        eventsRepository.sendStartTyping(888)

        receivedTypingLatch.await(3, TimeUnit.SECONDS)

        assertEquals(0, receivedTypingLatch.count)

        assertEquals("John and 2 others are typing...", actualTypingNames)

        CountDownLatch(3).await(7, TimeUnit.SECONDS)

        assertTrue(actualTypingNames.isEmpty())

        executeScope.cancel()
    }

    @Test
    @Ignore
    @ExperimentalCoroutinesApi
    fun executeAndStartTyping_stopTyping_receivedTyping() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity> {
                val users = mutableListOf<UserEntity>()

                val userA = buildUserEntity(555)
                userA.setName("Bob")
                users.add(userA)

                val userB = buildUserEntity(777)
                userB.setLogin("Michael")
                users.add(userB)

                val userC = buildUserEntity(888)
                userC.setName("John")
                users.add(userC)

                return users
            }
        }

        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }

            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val receivedTypingLatch = CountDownLatch(6)

        var actualTypingNames = ""

        val executeScope = launch(UnconfinedTestDispatcher()) {
            val useCase = TypingEventUseCase(buildDialogEntity())
            useCase.timerLength = 5000
            useCase.execute().collect { typingEntity ->
                actualTypingNames = typingEntity!!.getText()
                receivedTypingLatch.countDown()
            }
        }

        eventsRepository.sendStartTyping(555)
        eventsRepository.sendStartTyping(777)
        eventsRepository.sendStartTyping(888)

        receivedTypingLatch.await(3, TimeUnit.SECONDS)

        assertEquals("John and 2 others are typing...", actualTypingNames)

        eventsRepository.sendStopTyping(555)

        receivedTypingLatch.await(1, TimeUnit.SECONDS)

        assertEquals("John and 1 others are typing...", actualTypingNames)

        eventsRepository.sendStopTyping(777)

        assertEquals("John typing...", actualTypingNames)

        eventsRepository.sendStopTyping(888)

        receivedTypingLatch.await(1, TimeUnit.SECONDS)

        assertTrue(actualTypingNames.isEmpty())

        executeScope.cancel()
    }

    private fun buildDialogEntity(): DialogEntity {
        val dialogEntity = DialogEntityImpl()
        dialogEntity.setParticipantIds(mutableListOf(777, 888))

        return dialogEntity
    }
}