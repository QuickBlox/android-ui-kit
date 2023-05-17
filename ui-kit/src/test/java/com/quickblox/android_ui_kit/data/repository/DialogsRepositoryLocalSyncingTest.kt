/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
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

class DialogsRepositoryLocalSyncingTest : BaseTest() {
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
    fun notSynced_syncTwice_syncedSuccess() = runTest {
        val dialogsRepository = DialogsRepositorySpy()

        val syncedLatch = CountDownLatch(2)
        val notSyncedLatch = CountDownLatch(2)

        val scope = launch {
            dialogsRepository.subscribeLocalSyncing().collect { synced ->
                if (synced) {
                    syncedLatch.countDown()
                } else {
                    notSyncedLatch.countDown()
                }
            }
        }

        delay(1000)
        dialogsRepository.setLocalSynced(true)

        delay(1000)
        dialogsRepository.setLocalSynced(false)

        delay(1000)
        dialogsRepository.setLocalSynced(true)

        delay(1000)
        scope.cancel()

        assertEquals(0, syncedLatch.count)
        assertEquals(0, notSyncedLatch.count)

        val synced = dialogsRepository.subscribeLocalSyncing().first()
        assertEquals(true, synced)
    }
}