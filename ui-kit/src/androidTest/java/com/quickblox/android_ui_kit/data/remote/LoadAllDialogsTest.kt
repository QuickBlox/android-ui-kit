/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.remote

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.request.QBRequestGetBuilder
import junit.framework.Assert.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LoadAllDialogsTest : BaseTest() {
    private val remoteDataSource: RemoteDataSource = buildRemoteDataSource(5)

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
    fun existConnection_loadAllDialogs_onComplete() {
        val successDTODialogs = mutableListOf<RemoteDialogDTO?>()

        val listenerLatch = CountDownLatch(1)

        CoroutineScope(Dispatchers.IO).launch {
            remoteDataSource.getAllDialogs().catch { e ->
                fail("expected: collect, actual: catch, error: $e")
            }.onCompletion {
                listenerLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    successDTODialogs.add(result.getOrNull())
                }
                if (result.isFailure) {
                    fail("expected: success, actual: failure, details: ${result.exceptionOrNull()}")
                }
            }
        }

        listenerLatch.await(1, TimeUnit.MINUTES)
        assertEquals(0, listenerLatch.count)

        assertTrue(successDTODialogs.isNotEmpty())
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
}