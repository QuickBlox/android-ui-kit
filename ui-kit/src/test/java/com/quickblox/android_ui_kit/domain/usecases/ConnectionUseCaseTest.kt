/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ConnectionUseCaseTest : BaseTest() {
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
        val connectionRepository = ConnectionRepositorySpy(existConnection = false)

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

        connectionRepository.connect()

        assertEquals(0, disconnectedLatch.count)

        scope.cancel()
    }
}