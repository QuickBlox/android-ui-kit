/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.DEFAULT_DELAY
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException.Codes.INCORRECT_DATA
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException.Codes.UNAUTHORISED
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.repository.DialogsRepositoryStub
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CreatePrivateDialogUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun createRemoteAndSaveLocal_execute_onComplete() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return CreateRemoteAndSaveLocalRepositoryStub()
            }

            override fun getUsersRepository(): UsersRepository {
                return UsersRepositorySpy()
            }
        })

        var createdDialog: DialogEntity? = null
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreatePrivateDialogUseCase(8888).execute()
            }.onSuccess { result ->
                createdDialog = result
            }.onFailure { error ->
                Assert.fail("expected: Exception, actual: NotException")
            }
        }

        assertTrue(createdDialog?.getDialogId()!!.isNotEmpty())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createRemoteThrowException_execute_onError() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return CreateRemoteThrowExceptionRepositoryStub()
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreatePrivateDialogUseCase(8888).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }
        errorLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(errorLatch.count, 0)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun saveLocalThrowException_execute_onError() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return SaveLocalThrowExceptionRepositoryStub()
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreatePrivateDialogUseCase(8888).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }
        errorLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(errorLatch.count, 0)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createRemoteAndSaveLocalBothThrowException_execute_onError() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return CreateRemoteAndSaveLocalBothThrowExceptionRepositoryStub()
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreatePrivateDialogUseCase(8888).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }
        errorLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(errorLatch.count, 0)
    }

    private inner class CreateRemoteThrowExceptionRepositoryStub : CreateRemoteAndSaveLocalRepositoryStub() {
        override fun createDialogInRemote(entity: DialogEntity): DialogEntity {
            throw DialogsRepositoryException(UNAUTHORISED, "")
        }
    }

    private inner class SaveLocalThrowExceptionRepositoryStub : CreateRemoteAndSaveLocalRepositoryStub() {
        override suspend fun saveDialogToLocal(entity: DialogEntity) {
            throw DialogsRepositoryException(UNAUTHORISED, "")
        }
    }

    open inner class CreateRemoteAndSaveLocalRepositoryStub : DialogsRepositoryStub() {
        override fun createDialogInRemote(entity: DialogEntity): DialogEntity {
            return DialogEntitySpy()
        }

        override suspend fun saveDialogToLocal(entity: DialogEntity) {}
    }

    private inner class CreateRemoteAndSaveLocalBothThrowExceptionRepositoryStub : DialogsRepositoryStub() {
        override fun createDialogInRemote(entity: DialogEntity): DialogEntity {
            throw DialogsRepositoryException(UNAUTHORISED, "")
        }

        override suspend fun saveDialogToLocal(entity: DialogEntity) {
            throw DialogsRepositoryException(INCORRECT_DATA, "")
        }
    }
}