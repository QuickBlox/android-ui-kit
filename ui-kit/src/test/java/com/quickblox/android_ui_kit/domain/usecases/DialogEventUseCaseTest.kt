/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
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

class DialogEventUseCaseTest : BaseTest() {
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

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun createUseCaseWithEmptyDialogId_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        DialogEventUseCase("").execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialog_executeAndSendDialog_dialogExist() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val dialog = DialogEntitySpy()

        val dialogLatch = CountDownLatch(1)
        val executeScope = launch(UnconfinedTestDispatcher()) {
            DialogEventUseCase(dialog.getDialogId()!!).execute().collect {
                assertEquals(dialog.getDialogId(), it?.getDialogId())
                dialogLatch.countDown()
            }
        }

        eventsRepository.sendDialog(dialog)

        dialogLatch.await(3, TimeUnit.SECONDS)

        assertEquals(0, dialogLatch.count)

        executeScope.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialog_executeAndSendAnotherDialog_notReceivedDialog() = runTest {
        val eventsRepository = EventsRepositorySpy()

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getEventsRepository(): EventsRepository {
                return eventsRepository
            }
        })

        val executeScope = launch(UnconfinedTestDispatcher()) {
            val dialog = DialogEntitySpy()
            DialogEventUseCase(dialog.getDialogId()!!).execute().collect {
                fail("expected: Nothing, actual: Received dialog")
            }
        }

        eventsRepository.sendDialog(DialogEntitySpy())

        executeScope.cancel()
    }
}