/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.EventsRepositoryException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.EventsRepositorySpy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class StopTypingEventUseCaseTest : BaseTest() {
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
    fun existDialogId_execute_receivedNoError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val dialogId = System.currentTimeMillis().toString()
        StopTypingEventUseCase(dialogId).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun dialogIdIsEmpty_execute_receivedError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        StopTypingEventUseCase("").execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun getDialogFromLocalThrowException_execute_receivedError() = runTest {
        val dialogsRepository = object : DialogsRepositorySpy() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })

        val dialogId = System.currentTimeMillis().toString()
        StopTypingEventUseCase(dialogId).execute()
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun sendStopTypingEventThrowException_execute_receivedError() = runTest {
        val eventRepository = object : EventsRepositorySpy() {
            override fun stopTypingEvent(dialogEntity: DialogEntity) {
                throw EventsRepositoryException(EventsRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getEventsRepository(): EventsRepository {
                return eventRepository
            }
        })

        val dialogId = System.currentTimeMillis().toString()
        StopTypingEventUseCase(dialogId).execute()
    }
}