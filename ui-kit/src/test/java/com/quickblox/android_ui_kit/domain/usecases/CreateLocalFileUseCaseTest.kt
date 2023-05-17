/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.DEFAULT_DELAY
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.spy.repository.FileRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CreateLocalFileUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun repositoryHasFileEntity_execute_entityExist() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getFilesRepository(): FilesRepository {
                return FileRepositorySpy()
            }
        })

        var file: FileEntity? = null

        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreateLocalFileUseCase("jpg").execute()
            }.onSuccess { result ->
                file = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        assertTrue(file?.getFile() != null)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun repositoryThrowException_execute_exceptionExist() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getFilesRepository(): FilesRepository {
                return object : FileRepositorySpy() {
                    override fun createLocalFile(extension: String): FileEntity {
                        throw FilesRepositoryException(FilesRepositoryException.Codes.INCORRECT_DATA, "incorrect data")
                    }
                }
            }
        })

        val listenerLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreateLocalFileUseCase("jpg").execute()
            }.onSuccess { result ->
                fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                listenerLatch.countDown()
            }
        }

        listenerLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(listenerLatch.count, 0)
    }
}