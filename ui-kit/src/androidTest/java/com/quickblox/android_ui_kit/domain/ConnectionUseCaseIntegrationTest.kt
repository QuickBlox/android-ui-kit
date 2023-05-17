/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.connection.ConnectionRepositoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.chat.QBChatService
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ConnectionUseCaseIntegrationTest : BaseTest() {
    @Before
    fun init() {
        initQuickblox()
        loginToRest()
    }

    @After
    fun release() {
        logoutFromChat()
        logoutFromRest()
    }

    // TODO: need to fix test. This test destroy DialogsEventUseCaseIntegrationTest
    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun notConnected_connect_connected() = runBlocking {
        val connectionRepository = ConnectionRepositoryImpl(RemoteDataSourceImpl())

        val connectedLatch = CountDownLatch(1)

        val connectRepositoryScope = launch(Dispatchers.Main) {
            connectionRepository.subscribe().collect { connected ->
                if (connected) {
                    connectedLatch.countDown()
                }
            }
        }

        setDependency(connectionRepository)

        val connectionScope = launch(Dispatchers.Main) {
            ConnectionUseCase().execute()
        }

        connectedLatch.await(20, TimeUnit.SECONDS)
        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        assertTrue(QBChatService.getInstance().isLoggedIn)
        assertEquals(0, connectedLatch.count)

        connectionScope.cancel()
        connectRepositoryScope.cancel()
    }

    // TODO: need to fix test. This test destroy DialogsEventUseCaseIntegrationTest
    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun notConnected_ConnectAndDisconnect_disconnected() = runBlocking {
        val connectionRepository = ConnectionRepositoryImpl(RemoteDataSourceImpl())

        val connectedLatch = CountDownLatch(1)
        val disconnectedLatch = CountDownLatch(2)

        val connectSubscriptionScope = launch(Dispatchers.Main) {
            connectionRepository.subscribe().collect { connected ->
                if (connected) {
                    connectedLatch.countDown()
                } else {
                    disconnectedLatch.countDown()
                }
            }
        }

        setDependency(connectionRepository)

        val connectionScope = launch(Dispatchers.Main) {
            ConnectionUseCase().execute()
        }

        connectedLatch.await(20, TimeUnit.SECONDS)
        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        assertTrue(QBChatService.getInstance().isLoggedIn)

        disconnectedLatch.await(20, TimeUnit.SECONDS)
        CountDownLatch(1).await(3, TimeUnit.SECONDS)

        assertFalse(QBChatService.getInstance().isLoggedIn)

        assertEquals(0, connectedLatch.count)
        assertEquals(0, disconnectedLatch.count)

        connectionScope.cancel()
        connectSubscriptionScope.cancel()
    }

    private fun setDependency(connectionRepository: ConnectionRepository) {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })
    }
}