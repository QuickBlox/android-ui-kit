/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import com.quickblox.android_ui_kit.spy.repository.ConnectionRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class EventsRepositoryTest : BaseTest() {
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

    /*@Test
    @ExperimentalCoroutinesApi
    fun connectThrowsExceptionUNAUTHORIZED_connect_receivedException() = runTest {
        val description = System.currentTimeMillis().toString()
        val eventsRepository = object : EventsRepositorySpy() {
            override fun startTypingEvent(dialogEntity: DialogEntity) {
                throw RemoteDataSourceException(RemoteDataSourceException.Codes)
            }

            override suspend fun connect() {
                throw ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNAUTHORIZED, description)
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return eventsRepository
            }
        })

        try {
            eventsRepository.connect()
            fail()
        } catch (e: ConnectionRepositoryException) {
            assertEquals(e.code, ConnectionRepositoryException.Codes.UNAUTHORIZED)
            assertEquals(e.description, description)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun disconnectThrowsExceptionUNAUTHORIZED_connect_receivedException() = runTest {
        val description = System.currentTimeMillis().toString()
        val connectionRepository = object : ConnectionRepositorySpy() {
            override suspend fun disconnect() {
                throw ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNAUTHORIZED, description)
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }
        })

        try {
            connectionRepository.disconnect()
            fail()
        } catch (e: ConnectionRepositoryException) {
            assertEquals(e.code, ConnectionRepositoryException.Codes.UNAUTHORIZED)
            assertEquals(e.description, description)
        }
    }*/
}