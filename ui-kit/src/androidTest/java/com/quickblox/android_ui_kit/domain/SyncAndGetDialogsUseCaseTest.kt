/*
 * Created by Injoit on 4.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.dialog.DialogsRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.usecases.GetDialogsUseCase
import com.quickblox.android_ui_kit.domain.usecases.SyncDialogsUseCase
import com.quickblox.android_ui_kit.spy.ConnectionRepositorySpy
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.request.QBRequestGetBuilder
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onCompletion
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

@Ignore("need to fix coroutine/flow behaviour")
@RunWith(AndroidJUnit4::class)
class SyncAndGetDialogsUseCaseTest : BaseTest() {
    private val loadDialogsCount = 10
    private val remoteDataSource = buildRemoteDataSource(loadDialogsCount)
    private val dialogsRepository = DialogsRepositoryImpl(remoteDataSource, LocalDataSourceImpl())
    private val connectionRepository = ConnectionRepositorySpy(existConnection = true)

    private fun buildRemoteDataSource(loadedDialogsCount: Int): RemoteDataSource {
        return object : RemoteDataSourceImpl() {
            override fun loadAllQBDialogs(requestBuilder: QBRequestGetBuilder): List<QBChatDialog> {
                val modifiedRequestBuilder = QBRequestGetBuilder()
                modifiedRequestBuilder.limit = loadedDialogsCount

                return super.loadAllQBDialogs(modifiedRequestBuilder)
            }
        }
    }

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()
    }

    private fun initDependency() {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }

            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })
    }

    @After
    fun release() {
        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existConnection_syncDialogsAndGetDialogs_entitiesExists() = runBlocking {
        dialogsRepository.setLocalSynced(false)

        val syncDialogsScope = launch(Dispatchers.IO) {
            SyncDialogsUseCase().execute()
        }

        CountDownLatch(1).await(5, TimeUnit.SECONDS)

        val loadDialogsCompleteLatch = CountDownLatch(1)
        val loadedDialogs = hashSetOf<DialogEntity>()
        launch(Dispatchers.IO) {
            GetDialogsUseCase().execute().onCompletion {
                loadDialogsCompleteLatch.countDown()
            }.collect { result ->
                loadedDialogs.add(result.getOrThrow())
            }
        }

        loadDialogsCompleteLatch.await(20, TimeUnit.SECONDS)

        syncDialogsScope.cancel()

        assertEquals(loadDialogsCount, loadedDialogs.size)

        assertEquals(0, loadDialogsCompleteLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun notExistConnection_syncDialogsAndGetDialogsAndConnectionExist_entitiesExists() = runBlocking {
        connectionRepository.setConnection(false)

        val syncDialogsScope = launch(Dispatchers.IO) {
            SyncDialogsUseCase().execute()
        }

        CountDownLatch(1).await(5, TimeUnit.SECONDS)

        val loadedDialogs = hashSetOf<DialogEntity>()

        val loadDialogsCompleteLatch = CountDownLatch(1)
        val clearDialogsLatch = CountDownLatch(1)
        launch(Dispatchers.IO) {
            GetDialogsUseCase().execute().onCompletion {
                loadDialogsCompleteLatch.countDown()
            }.collect { result ->
                if (result.isFailure) {
                    loadedDialogs.clear()
                    clearDialogsLatch.countDown()
                } else {
                    loadedDialogs.add(result.getOrThrow())
                }
            }
        }

        changeConnectionWithDelay(true, 5)

        clearDialogsLatch.await(10, TimeUnit.SECONDS)

        loadDialogsCompleteLatch.await(20, TimeUnit.SECONDS)

        syncDialogsScope.cancel()

        assertEquals(loadDialogsCount, loadedDialogs.size)

        assertEquals(0, loadDialogsCompleteLatch.count)
        assertEquals(0, clearDialogsLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existConnection_syncDialogsAndGetDialogsAndDropConnection_entitiesExists() = runBlocking {
        connectionRepository.setConnection(true)

        val syncDialogsScope = launch(Dispatchers.IO) {
            SyncDialogsUseCase().execute()
        }

        CountDownLatch(1).await(5, TimeUnit.SECONDS)

        val loadedDialogs = hashSetOf<DialogEntity>()

        val loadDialogsCompleteLatch = CountDownLatch(1)
        val clearDialogsLatch = CountDownLatch(1)
        launch(Dispatchers.IO) {
            GetDialogsUseCase().execute().onCompletion {
                loadDialogsCompleteLatch.countDown()
            }.collect { dialogEntity ->
                if (dialogEntity.isFailure) {
                    loadedDialogs.clear()
                    clearDialogsLatch.countDown()
                } else {
                    loadedDialogs.add(dialogEntity.getOrThrow())
                    Log.d("NOTIFY_DIALOG_TAG", "received dialog: ${dialogEntity.getOrThrow()}")
                }
            }
        }

        changeConnectionWithDelay(false, 5)

        clearDialogsLatch.await(10, TimeUnit.SECONDS)

        changeConnectionWithDelay(true, 5)

        loadDialogsCompleteLatch.await(20, TimeUnit.SECONDS)

        syncDialogsScope.cancel()

        assertEquals(loadDialogsCount, loadedDialogs.size)

        assertEquals(0, loadDialogsCompleteLatch.count)
        assertEquals(0, clearDialogsLatch.count)
    }

    private fun changeConnectionWithDelay(connected: Boolean, secondsDelay: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            Timer().schedule(timerTask {
                CoroutineScope(Dispatchers.IO).launch {
                    connectionRepository.setConnection(connected)
                }
            }, secondsDelay * 1000)
        }
    }
}