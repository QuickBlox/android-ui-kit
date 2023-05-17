/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch

class AddUsersToDialogUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun have3UserNames_createMessageText_received3Names() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val loggedUserName = "Alisa"

        val userNames = "John, Mark, Michael"

        val useCase = AddUsersToDialogUseCase(DialogEntitySpy(), arrayListOf())
        val createdMessageText = useCase.createMessageText(loggedUserName, userNames)

        val expectedMessageText = "User $loggedUserName added users $userNames"
        assertEquals(expectedMessageText, createdMessageText)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun have1UserNames_createMessageText_received1Names() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val loggedUserName = "Alisa"

        val userName = "John"

        val useCase = AddUsersToDialogUseCase(DialogEntitySpy(), arrayListOf())
        val createdMessageText = useCase.createMessageText(loggedUserName, userName)

        val expectedMessageText = "User $loggedUserName added user $userName"
        assertEquals(expectedMessageText, createdMessageText)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun haveMessageTextAndDialogId_createEvent_receivedEvent() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val useCase = AddUsersToDialogUseCase(DialogEntitySpy(), arrayListOf())
        val eventText = "${System.currentTimeMillis()}"
        val dialogId = "${System.currentTimeMillis()}"
        val createdEvent = useCase.createEvent(eventText, dialogId)

        assertEquals(EventMessageEntity.EventTypes.ADDED_USER_TO_DIALOG, createdEvent.getEventType())
        assertEquals(eventText, createdEvent.getText())
        assertEquals(dialogId, createdEvent.getDialogId())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithOneUser_addTwoUsersAndExecute_receivedDialogWithTwoUsers() = runTest {
        val loggedUserId = 888

        val createdParticipantIds = arrayListOf(777, loggedUserId)
        val createdDialog = buildGroupDialogEntity(createdParticipantIds)

        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
                val updatedOccupantIds = arrayListOf<Int>()
                updatedOccupantIds.addAll(createdParticipantIds)
                updatedOccupantIds.addAll(userIds)

                return buildGroupDialogEntity(updatedOccupantIds)
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        var updatedDialog: DialogEntity? = null
        val addedParticipantIds = arrayListOf(111, 222)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                AddUsersToDialogUseCase(createdDialog, addedParticipantIds).execute()
            }.onSuccess { result ->
                updatedDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception, details $error")
            }
        }

        val expectedParticipantCount = createdParticipantIds.size + addedParticipantIds.size
        assertEquals(expectedParticipantCount, updatedDialog?.getParticipantIds()!!.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithZeroUsers_addTwoUsersAndExecute_receivedDialogWithTwoUsers() = runTest {
        val loggedUserId = 888

        val createdParticipantIds = arrayListOf(loggedUserId)
        val createdDialog = buildGroupDialogEntity(createdParticipantIds)

        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
                val updatedOccupantIds = arrayListOf<Int>()
                updatedOccupantIds.addAll(createdParticipantIds)
                updatedOccupantIds.addAll(userIds)

                return buildGroupDialogEntity(updatedOccupantIds)
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        var updatedDialog: DialogEntity? = null
        val addedParticipantIds = arrayListOf(111, 222)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                AddUsersToDialogUseCase(createdDialog, addedParticipantIds).execute()
            }.onSuccess { result ->
                updatedDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception, details $error")
            }
        }

        val expectedParticipantCount = createdParticipantIds.size + addedParticipantIds.size
        assertEquals(expectedParticipantCount, updatedDialog?.getParticipantIds()!!.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getLoggedUserReturnException_addTwoUsersAndExecute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                throw UsersRepositoryException(UsersRepositoryException.Codes.UNAUTHORISED, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val createdDialog = buildGroupDialogEntity(arrayListOf(888))
                AddUsersToDialogUseCase(createdDialog, arrayListOf(777)).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NoException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun addUsersToDialogThrowException_addTwoUsersAndExecute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                return 888
            }
        }

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.RESTRICTED_ACCESS, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val createdDialog = buildGroupDialogEntity(arrayListOf(888))
                AddUsersToDialogUseCase(createdDialog, arrayListOf(777)).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NoException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithOneUser_addTwoUsersWithLoggedIdAndExecute_receivedException() = runTest {
        val loggedUserId = 888

        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val createdDialog = buildGroupDialogEntity(arrayListOf(loggedUserId))
                AddUsersToDialogUseCase(createdDialog, arrayListOf(loggedUserId)).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NoException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createPrivateDialogWithOneUser_addTwoUsersWithLoggedIdAndExecute_receivedException() = runTest {
        val loggedUserId = 888

        val usersRepository = object : UsersRepositorySpy() {
            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val createdDialog = buildGroupDialogEntity(arrayListOf(loggedUserId))
                createdDialog.setDialogType(DialogEntity.Types.PRIVATE)
                AddUsersToDialogUseCase(createdDialog, arrayListOf(loggedUserId)).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NoException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    private fun buildGroupDialogEntity(participantIds: List<Int>): DialogEntity {
        val dialog = DialogEntitySpy()
        dialog.setParticipantIds(participantIds)
        dialog.setDialogType(DialogEntity.Types.GROUP)

        return dialog
    }
}