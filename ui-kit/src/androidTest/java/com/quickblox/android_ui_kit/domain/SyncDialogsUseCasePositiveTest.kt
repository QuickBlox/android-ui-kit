/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.dialog.DialogsRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.usecases.SyncDialogsUseCase
import com.quickblox.android_ui_kit.spy.ConnectionRepositorySpy
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.request.QBRequestGetBuilder
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SyncDialogsUseCasePositiveTest : BaseTest() {
    @Before
    fun init() {
        initQuickblox()
        loginToRest()
        loginToChat()
    }

    @After
    fun release() {
        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existConnection_execute_entitiesExists() = runBlocking {
        val loadedDialogsCount = 5
        val remoteDataSource = buildRemoteDataSource(loadedDialogsCount)

        val syncedCacheLatch = CountDownLatch(1)
        val dialogsRepository = object : DialogsRepositoryImpl(remoteDataSource, LocalDataSourceImpl()) {
            override suspend fun setLocalSynced(synced: Boolean) {
                super.setLocalSynced(synced)
                if (synced) {
                    syncedCacheLatch.countDown()
                }
            }
        }

        setDependency(dialogsRepository, ConnectionRepositorySpy(existConnection = true))

        val scope = launch(Dispatchers.Main) {
            SyncDialogsUseCase().execute()
        }

        syncedCacheLatch.await(10, TimeUnit.SECONDS)
        checkCacheSyncedAndContainsDialogs(dialogsRepository, loadedDialogsCount)

        assertEquals(0, syncedCacheLatch.count)
        scope.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existConnection_disconnectAndConnect_entitiesExists() = runBlocking {
        val loadedDialogsCount = 5
        val remoteDataSource = buildRemoteDataSource(loadedDialogsCount)

        val syncedCacheLatch = CountDownLatch(2)
        val dialogsRepository = object : DialogsRepositoryImpl(remoteDataSource, LocalDataSourceImpl()) {
            override suspend fun setLocalSynced(synced: Boolean) {
                super.setLocalSynced(synced)
                if (synced) {
                    syncedCacheLatch.countDown()
                }
            }
        }

        val connectionRepository = ConnectionRepositorySpy(existConnection = true)
        setDependency(dialogsRepository, connectionRepository)

        val scope = launch(Dispatchers.Main) {
            SyncDialogsUseCase().execute()
        }

        syncedCacheLatch.await(10, TimeUnit.SECONDS)
        checkCacheSyncedAndContainsDialogs(dialogsRepository, loadedDialogsCount)

        connectionRepository.setConnection(false)

        syncedCacheLatch.await(2, TimeUnit.SECONDS)
        checkCacheNoSyncedAndNotContainsDialogs(dialogsRepository)

        connectionRepository.setConnection(true)

        syncedCacheLatch.await(10, TimeUnit.SECONDS)
        checkCacheSyncedAndContainsDialogs(dialogsRepository, loadedDialogsCount)

        assertEquals(0, syncedCacheLatch.count)

        scope.cancel()
    }

    private suspend fun checkCacheSyncedAndContainsDialogs(dialogsRepository: DialogsRepository, dialogsCount: Int) {
        assertTrue(dialogsRepository.subscribeLocalSyncing().first())
        assertEquals(dialogsCount, dialogsRepository.getAllDialogsFromLocal().size)
    }

    private suspend fun checkCacheNoSyncedAndNotContainsDialogs(dialogsRepository: DialogsRepository) {
        assertFalse(dialogsRepository.subscribeLocalSyncing().first())
        assertTrue(dialogsRepository.getAllDialogsFromLocal().isEmpty())
    }

    private fun buildRemoteDataSource(loadedDialogsCount: Int): RemoteDataSource {
        return object : RemoteDataSourceImpl() {
            override fun loadAllQBDialogs(requestBuilder: QBRequestGetBuilder): List<QBChatDialog> {
                val modifiedRequestBuilder = QBRequestGetBuilder()
                modifiedRequestBuilder.limit = loadedDialogsCount
                return super.loadAllQBDialogs(modifiedRequestBuilder)
            }
        }
    }

    private fun setDependency(dialogsRepository: DialogsRepository, connectionRepository: ConnectionRepository) {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }

            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })
    }
}