/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.DEFAULT_DELAY
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException.Codes.UNAUTHORISED
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.entity.DialogEntityStub
import com.quickblox.android_ui_kit.stub.entity.UserEntityStub
import com.quickblox.android_ui_kit.stub.repository.DialogsRepositoryStub
import junit.framework.Assert.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LeaveDialogUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun haveUserName_createTextMessage_receivedName() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = LeaveDialogUseCase(createDialogEntity("Test dialogId"))

        val userName = "Bob"
        val createdMessageText = useCase.createMessageText(userName)

        val expectedMessageText = "User Bob left"
        assertEquals(expectedMessageText, createdMessageText)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun userOnlyWithLogin_getUserName_receivedLogin() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = LeaveDialogUseCase(createDialogEntity("Test dialogId"))

        val userLogin = "Bob login"

        val loggedUser = object : UserEntityStub() {
            override fun getName(): String? {
                return null
            }

            override fun getLogin(): String {
                return userLogin
            }
        }
        val gotUserName = useCase.getUserNameFrom(loggedUser)

        assertEquals(userLogin, gotUserName)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun userOnlyWithName_getUserName_receivedName() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = LeaveDialogUseCase(createDialogEntity("Test dialogId"))

        val loggedUserName = "Bob"

        val loggedUser = object : UserEntityStub() {
            override fun getName(): String {
                return loggedUserName
            }
        }
        val gotUserName = useCase.getUserNameFrom(loggedUser)

        assertEquals(loggedUserName, gotUserName)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveMessageTextAndDialogId_createEvent_receivedEvent() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = LeaveDialogUseCase(createDialogEntity("Test dialogId"))

        val eventText = "${System.currentTimeMillis()}"
        val dialogId = "${System.currentTimeMillis()}"
        val createdEvent = useCase.createEvent(eventText, dialogId)

        assertEquals(EventMessageEntity.EventTypes.LEFT_USER_FROM_DIALOG, createdEvent.getEventType())
        assertEquals(eventText, createdEvent.getText())
        assertEquals(dialogId, createdEvent.getDialogId())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun leaveDialog_execute_onComplete() = runTest {
        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getDialogsRepository(): DialogsRepository {
                return object : DialogsRepositoryStub() {
                    override fun leaveDialogFromRemote(entity: DialogEntity) {
                        // empty
                    }

                    override suspend fun deleteDialogFromLocal(dialogId: String) {
                        // empty
                    }
                }
            }
        })

        val dialogEntity = createDialogEntity("Test dialogId")
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                LeaveDialogUseCase(dialogEntity).execute()
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun throwExceptionUnauthorised_execute_onError() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return object : DialogsRepositoryStub() {
                    override fun leaveDialogFromRemote(entity: DialogEntity) {
                        throw DialogsRepositoryException(UNAUTHORISED, "")
                    }
                }
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                LeaveDialogUseCase(DialogEntityStub()).execute()
            }.onSuccess { result ->
                fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        errorLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(errorLatch.count, 0)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createEntityWithNullId_execute_onError() = runTest {
        val dialogEntity = createDialogEntity(null)

        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                LeaveDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                fail("expected:  Result.Failure, actual: Result.Success")
            }.onFailure { error ->
                assertTrue(true)
            }
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createEntityWithEmptyId_execute_onError() = runTest {
        val dialogEntity = createDialogEntity("")

        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                LeaveDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                fail("expected:  Result.Failure, actual: Result.Success")
            }.onFailure { error ->
                assertTrue(true)
            }
        }
    }

    private fun createDialogEntity(id: String?): DialogEntity {
        return object : DialogEntityStub() {
            override fun getDialogId(): String? {
                return id
            }
        }
    }
}