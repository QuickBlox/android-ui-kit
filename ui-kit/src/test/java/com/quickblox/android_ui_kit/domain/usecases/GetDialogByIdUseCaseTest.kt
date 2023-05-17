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
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.entity.DialogEntityStub
import com.quickblox.android_ui_kit.stub.repository.DialogsRepositoryStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch

class GetDialogByIdUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun localCacheSynced_execute_receivedDialog() = runTest {
        val dialogId = UUID.randomUUID().toString()

        val dialogEntity = object : DialogEntityStub() {
            override fun getDialogId(): String {
                return dialogId
            }
        }

        val dialogRepository = object : DialogsRepositoryStub() {
            override fun subscribeLocalSyncing(): Flow<Boolean> {
                return flow {
                    emit(true)
                }
            }

            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                return dialogEntity
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        var loadedDialog: DialogEntity? = null
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogByIdUseCase(dialogId).execute()
            }.onSuccess { result ->
                loadedDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NotException, actual: Exception, details $error")
            }
        }

        assertEquals(dialogId, loadedDialog?.getDialogId()!!)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun localCacheNotSynced_execute_receivedDialog() = runTest {
        val dialogId = UUID.randomUUID().toString()

        val dialogEntity = object : DialogEntityStub() {
            override fun getDialogId(): String {
                return dialogId
            }
        }

        val dialogRepository = object : DialogsRepositoryStub() {
            override fun subscribeLocalSyncing(): Flow<Boolean> {
                return flow {
                    emit(false)
                }
            }

            override fun getDialogFromRemote(dialogId: String): DialogEntity {
                return dialogEntity
            }

            override suspend fun updateDialogInLocal(entity: DialogEntity) {

            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        var loadedDialog: DialogEntity? = null
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogByIdUseCase(dialogId).execute()
            }.onSuccess { result ->
                loadedDialog = result
            }.onFailure { error ->
                Assert.fail("expected: NotException, actual: Exception, details $error")
            }
        }

        assertEquals(dialogId, loadedDialog?.getDialogId()!!)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun dialogNotExistAndThrowNotFoundException_execute_receivedException() = runTest {
        val dialogRepository = object : DialogsRepositoryStub() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        val exceptionLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogByIdUseCase("dialog_id").execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                exceptionLatch.countDown()
            }
        }

        assertEquals(0, exceptionLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun dialogNotExistAndThrowIncorrectDataException_execute_receivedException() = runTest {
        val dialogRepository = object : DialogsRepositoryStub() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        val exceptionLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogByIdUseCase("dialog_id").execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                exceptionLatch.countDown()
            }
        }

        assertEquals(0, exceptionLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun dialogExist_executeWithEmptyDialogId_receivedNotFoundException() = runTest {
        val dialogId = UUID.randomUUID().toString()

        val dialogEntity = object : DialogEntityStub() {
            override fun getDialogId(): String {
                return dialogId
            }
        }

        val dialogRepository = object : DialogsRepositoryStub() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                return dialogEntity
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogRepository
            }
        })

        val exceptionLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogByIdUseCase("").execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                exceptionLatch.countDown()
            }
        }

        assertEquals(0, exceptionLatch.count)
    }
}