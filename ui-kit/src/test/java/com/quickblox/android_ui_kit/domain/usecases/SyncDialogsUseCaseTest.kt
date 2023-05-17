/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.usecases.SyncDialogsUseCase
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

class SyncDialogsUseCaseTest : BaseTest() {
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
    fun existConnection_execute_entitiesExists() = runTest {
        val remoteDialogsCount = 50
        val connectionRepository = ConnectionRepositorySpy(existConnection = true)

        val syncedCacheLatch = CountDownLatch(1)
        val dialogsRepository = object : DialogsRepositorySpy(remoteDialogsCount = remoteDialogsCount) {
            override suspend fun setLocalSynced(synced: Boolean) {
                super.setLocalSynced(synced)
                if (synced) {
                    syncedCacheLatch.countDown()
                }
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })

        val scope = launch(UnconfinedTestDispatcher()) {
            SyncDialogsUseCase().execute()
        }

        syncedCacheLatch.await(2, TimeUnit.SECONDS)

        checkCacheSyncedAndContainsDialogs(dialogsRepository, remoteDialogsCount)

        assertEquals(0, syncedCacheLatch.count)

        scope.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existConnection_disconnectedAndConnected_entitiesExists() = runTest {
        val remoteDialogsCount = 50
        val connectionRepository = ConnectionRepositorySpy(existConnection = true)

        val syncedCacheLatch = CountDownLatch(1)
        val dialogsRepository = object : DialogsRepositorySpy(remoteDialogsCount = remoteDialogsCount) {
            override suspend fun setLocalSynced(synced: Boolean) {
                super.setLocalSynced(synced)
                if (synced) {
                    syncedCacheLatch.countDown()
                }
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })

        val scope = CoroutineScope(Dispatchers.Main).launch {
            SyncDialogsUseCase().execute()
        }

        CoroutineScope(Dispatchers.Main).launch {
            connectionRepository.setConnection(false)

            CountDownLatch(1).await(1, TimeUnit.SECONDS)

            checkCacheNoSyncedAndNotContainsDialogs(dialogsRepository)

            connectionRepository.setConnection(true)

            CountDownLatch(1).await(1, TimeUnit.SECONDS)

            checkCacheSyncedAndContainsDialogs(dialogsRepository, remoteDialogsCount)

            assertEquals(0, syncedCacheLatch.count)

            scope.cancel()
        }
    }

    private suspend fun checkCacheSyncedAndContainsDialogs(dialogsRepository: DialogsRepository, dialogsCount: Int) {
        assertTrue(dialogsRepository.subscribeLocalSyncing().first())
        assertEquals(dialogsCount, dialogsRepository.getAllDialogsFromLocal().size)
    }

    private suspend fun checkCacheNoSyncedAndNotContainsDialogs(dialogsRepository: DialogsRepository) {
        assertFalse(dialogsRepository.subscribeLocalSyncing().first())
        assertTrue(dialogsRepository.getAllDialogsFromLocal().isEmpty())
    }
}