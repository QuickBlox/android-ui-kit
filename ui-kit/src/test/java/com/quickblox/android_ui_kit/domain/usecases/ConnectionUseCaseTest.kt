/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ConnectionUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun createUseCase_connect_noErrors() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        ConnectionUseCase().connect {
            fail()
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun connectThrowsException_connect_receivedException() = runTest {
        val connectionRepository = object : ConnectionRepositorySpy() {
            override suspend fun connect() {
                throw ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNEXPECTED, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        val exceptionLatch = CountDownLatch(1)
        ConnectionUseCase().connect {
            exceptionLatch.countDown()
        }

        exceptionLatch.await(1, TimeUnit.SECONDS)

        assertEquals(0, exceptionLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createUseCase_disconnect_noErrors() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        ConnectionUseCase().disconnect {
            fail()
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun disconnectThrowsException_disconnect_receivedException() = runTest {
        val connectionRepository = object : ConnectionRepositorySpy() {
            override suspend fun disconnect() {
                throw ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNEXPECTED, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        val exceptionLatch = CountDownLatch(1)
        ConnectionUseCase().disconnect() {
            exceptionLatch.countDown()
        }

        exceptionLatch.await(1, TimeUnit.SECONDS)

        assertEquals(0, exceptionLatch.count)
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