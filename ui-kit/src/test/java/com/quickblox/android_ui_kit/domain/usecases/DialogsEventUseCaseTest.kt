/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class DialogsEventUseCaseTest : BaseTest() {
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
    fun noErrors_executeAndSendEvent_dialogExist() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val dialogLatch = CountDownLatch(1)

        val executeScope = launch(UnconfinedTestDispatcher()) {
            DialogsEventUseCase().execute().collect {
                dialogLatch.countDown()
            }
        }

        CountDownLatch(1).await(1, TimeUnit.SECONDS)

        eventsRepository.sendDialog(DialogEntityImpl())

        dialogLatch.await(5, TimeUnit.SECONDS)

        assertEquals(0, dialogLatch.count)

        executeScope.cancel()
    }
}