/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onCompletion
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

class GetDialogsUseCaseTest : BaseTest() {
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
    fun syncedCache_execute_existDialogs() = runTest {
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

        val completeLatch = CountDownLatch(1)

        launch(UnconfinedTestDispatcher()) {
            GetDialogsUseCase().execute().onCompletion {
                completeLatch.countDown()
            }.collect { result ->
                loadedDialogs.add(result.getOrThrow())
            }
        }

        assertEquals(dialogsInCacheCount, loadedDialogs.size)
        assertEquals(0, completeLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun notSyncedCache_executeAndSyncCache_existDialogs() = runTest {
        val dialogsInCacheCount = 10

        val dialogsRepository = DialogsRepositorySpy(localDialogsCount = dialogsInCacheCount, synced = false)

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }

            override fun getConnectionRepository(): ConnectionRepository {
                return ConnectionRepositorySpy(existConnection = true)
            }
        })

        val loadedDialogs = mutableSetOf<DialogEntity>()

        val completeLatch = CountDownLatch(1)

        launch(UnconfinedTestDispatcher()) {
            GetDialogsUseCase().execute().onCompletion {
                completeLatch.countDown()
            }.collect { result ->
                loadedDialogs.add(result.getOrThrow())
            }
        }

        val addedDialogsCount = 5

        addDialogsToRepository(dialogsRepository, addedDialogsCount)

        dialogsRepository.setLocalSynced(true)

        val fullDialogsSize = dialogsInCacheCount + addedDialogsCount
        assertEquals(fullDialogsSize, loadedDialogs.size)
        assertEquals(0, completeLatch.count)
    }

    private suspend fun addDialogsToRepository(repository: DialogsRepository, countOfDialogs: Int) {
        repeat(countOfDialogs) {
            CountDownLatch(1).await(250, TimeUnit.MILLISECONDS)

            val dialogEntity = DialogEntityImpl()
            dialogEntity.setDialogId(System.currentTimeMillis().toString())

            repository.saveDialogToLocal(dialogEntity)
        }
    }
}