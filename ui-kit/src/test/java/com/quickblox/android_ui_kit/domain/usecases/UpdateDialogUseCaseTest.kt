/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.repository.UsersRepositoryStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch

class UpdateDialogUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun dialogExist_execute_receivedDialog() = runTest {
        val dialogId = UUID.randomUUID().toString()
        val createdDialog = DialogEntitySpy()
        createdDialog.setDialogId(dialogId)

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun updateDialogInRemote(entity: DialogEntity): DialogEntity {
                return createdDialog
            }
        }

        val usersRepository = object : UsersRepositoryStub() {
            override fun getLoggedUserId(): Int {
                return createdDialog.getOwnerId()!!
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }

            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        var loadedDialog: DialogEntity? = null
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UpdateDialogUseCase(createdDialog).execute()
            }.onSuccess { result ->
                loadedDialog = result
            }.onFailure { error ->
                Assert.fail("expected: Exception, actual: NotException")
            }
        }

        assertEquals(dialogId, loadedDialog?.getDialogId()!!)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun loggedWithNotOwnerId_execute_receivedException() = runTest {
        val dialogId = UUID.randomUUID().toString()
        val createdDialog = DialogEntitySpy()
        createdDialog.setDialogId(dialogId)

        val usersRepository = object : UsersRepositoryStub() {
            override fun getLoggedUserId(): Int {
                return 888888
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val failureLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UpdateDialogUseCase(createdDialog).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                failureLatch.countDown()
            }
        }

        assertEquals(0, failureLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existDialog_executeWithNullOwnerId_receivedException() = runTest {
        val createdDialog = DialogEntitySpy()
        createdDialog.setOwnerId(null)

        QuickBloxUiKit.setDependency(DependencySpy())

        val failureLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UpdateDialogUseCase(createdDialog).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                failureLatch.countDown()
            }
        }

        assertEquals(0, failureLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun updateInRemoteThrowException_execute_receivedException() = runTest {
        val createdDialog = DialogEntitySpy()

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun updateDialogInRemote(entity: DialogEntity): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.UNAUTHORISED, "")
            }
        }

        val usersRepository = object : UsersRepositoryStub() {
            override fun getLoggedUserId(): Int {
                return createdDialog.getOwnerId()!!
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }

            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val failureLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UpdateDialogUseCase(createdDialog).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                failureLatch.countDown()
            }
        }

        assertEquals(0, failureLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun updateInLocalThrowException_execute_receivedException() = runTest {
        val dialogId = UUID.randomUUID().toString()
        val createdDialog = DialogEntitySpy()
        createdDialog.setDialogId(dialogId)

        val dialogRepository = object : DialogsRepositorySpy() {
            override fun updateDialogInRemote(entity: DialogEntity): DialogEntity {
                return createdDialog
            }

            override suspend fun updateDialogInLocal(entity: DialogEntity) {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        val usersRepository = object : UsersRepositoryStub() {
            override fun getLoggedUserId(): Int {
                return createdDialog.getOwnerId()!!
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }

            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val failureLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UpdateDialogUseCase(createdDialog).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                failureLatch.countDown()
            }
        }

        assertEquals(0, failureLatch.count)
    }
}