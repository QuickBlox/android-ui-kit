/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.spy.DependencySpy
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class LoadUsersByNameWithExcludeByIdUseCaseTest : BaseTest() {
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
    fun existsUsers_execute_receiveUsers() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val loadedUsers = mutableListOf<UserEntity?>()

        val completedLatch = CountDownLatch(1)
        LoadUsersByNameWithExcludeByIdsUseCase(PaginationEntityImpl(), "test", arrayListOf(555, 777)).execute()
            .catch { error ->
                fail("expected: onCompletion, actual: catch, details: $error")
            }.onCompletion {
                completedLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                }
            }

        assertEquals(completedLatch.count, 0)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun existUsers_executeWithEmptyExclude_receiveUsers() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val completedLatch = CountDownLatch(1)
        LoadUsersByNameWithExcludeByIdsUseCase(PaginationEntityImpl(), "test", arrayListOf()).execute()
            .catch {
                completedLatch.countDown()
            }.collect { result ->
                fail("expected: Exception, actual: NoException")
            }

        assertEquals(completedLatch.count, 0)
    }
}