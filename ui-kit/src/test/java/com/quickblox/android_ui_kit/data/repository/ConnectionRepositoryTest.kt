/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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

class ConnectionRepositoryTest : BaseTest() {
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
    fun connectionNotExist_dropConnectionTwice_connectionExist() = runTest {
        val connectionRepository = ConnectionRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        val connectedLatch = CountDownLatch(2)
        val disconnectedLatch = CountDownLatch(2)

        val scope = launch {
            connectionRepository.subscribe().collect { connected ->
                if (connected) {
                    connectedLatch.countDown()
                } else {
                    disconnectedLatch.countDown()
                }
            }
        }

        delay(1000)
        connectionRepository.setConnection(true)

        delay(1000)
        connectionRepository.setConnection(false)

        delay(1000)
        connectionRepository.setConnection(true)

        delay(1000)
        scope.cancel()

        assertEquals(0, connectedLatch.count)
        assertEquals(0, disconnectedLatch.count)

        val connected = connectionRepository.subscribe().first()
        assertEquals(true, connected)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun notConnected_connect_connected() = runTest {
        val connectionRepository = ConnectionRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        val connectedLatch = CountDownLatch(1)

        val scope = launch(UnconfinedTestDispatcher()) {
            connectionRepository.subscribe().collect { connected ->
                if (connected) {
                    connectedLatch.countDown()
                }
            }
        }

        connectionRepository.connect()

        assertEquals(0, connectedLatch.count)

        scope.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun connected_disconnect_disconnected() = runTest {
        val connectionRepository = ConnectionRepositorySpy(existConnection = true)

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        val disconnectedLatch = CountDownLatch(1)

        val scope = launch(UnconfinedTestDispatcher()) {
            connectionRepository.subscribe().collect { connected ->
                val isNotConnected = !connected
                if (isNotConnected) {
                    disconnectedLatch.countDown()
                }
            }
        }

        connectionRepository.disconnect()

        assertEquals(0, disconnectedLatch.count)

        scope.cancel()
    }
}