/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GetDialogsByNameUseCaseTest : BaseTest() {
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
    fun syncedCache_execute_getDialogs() = runTest {
        val dialogsInCacheCount = 10

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return DialogsRepositorySpy(localDialogsCount = dialogsInCacheCount, synced = true)
            }

            override fun getConnectionRepository(): ConnectionRepository {
                return ConnectionRepositorySpy(existConnection = true)
            }
        })

        val loadedDialogs = mutableSetOf<DialogEntity>()

        val onSuccessLatch = CountDownLatch(1)

        launch(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogsByNameUseCase("name").execute()
            }.onSuccess { dialogs ->
                loadedDialogs.addAll(dialogs!!)
                onSuccessLatch.countDown()
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }
        onSuccessLatch.await(2, TimeUnit.SECONDS)

        assertEquals(dialogsInCacheCount, loadedDialogs.size)
        assertEquals(0, onSuccessLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun setDependency_executeWithEmptyName_DomainException() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return DialogsRepositorySpy()
            }
        })

        val onFailureLatch = CountDownLatch(1)

        launch(UnconfinedTestDispatcher()) {
            runCatching {
                GetDialogsByNameUseCase("").execute()
            }.onSuccess { dialogs ->
                fail("expected: Exception, actual: onSuccess")
            }.onFailure { error ->
                onFailureLatch.countDown()
            }

            onFailureLatch.await(2, TimeUnit.SECONDS)

            assertEquals(0, onFailureLatch.count)
        }
    }
}