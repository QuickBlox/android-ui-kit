/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.usecases.GetAllUsersWithExcludeByIdsUseCase
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class GetAllUsersWithExcludeByIdsUseCaseIntegrationTest : BaseTest() {
    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
        loginToChat()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    @After
    fun release() {
        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun paginationChangeToLoad10Users_executeWithExcludeOneUser_loaded9Users() = runBlocking {
        val completedLatch = CountDownLatch(1)

        val loadedUsers = mutableListOf<UserEntity?>()
        var loadedPagination: PaginationEntity? = null

        val users = QBUsers.getUsers(null).perform()
        val excludeId = users[0].id

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)
            initPagination.setCurrentPage(1)

            GetAllUsersWithExcludeByIdsUseCase(initPagination, arrayListOf(excludeId)).execute().catch { error ->
                fail("expected: onCompletion, actual: catch, details: $error")
            }.onCompletion {
                completedLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                    loadedPagination = result.getOrThrow().second
                }
                if (result.isFailure) {
                    fail("expected: Result.Success, actual: Result.Failure")
                }
            }
        }

        assertEquals(0, completedLatch.count)

        assertEquals(9, loadedUsers.size)

        assertEquals(loadedPagination!!.getPerPage(), 10)
        assertEquals(loadedPagination!!.getCurrentPage(), 1)
        assertTrue(loadedPagination!!.hasNextPage())

        assertTrue(usersNotContainsExcludeIds(arrayListOf(excludeId), loadedUsers))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun paginationChangeToLoad10Users_executeTwiceWithExcludeTwoUsers_loaded18Users() = runBlocking {
        val completedLatch = CountDownLatch(2)

        val loadedUsers = mutableListOf<UserEntity?>()
        var loadedPagination: PaginationEntity? = null

        val usersA = QBUsers.getUsers(null).perform()
        val excludeIdA = usersA[0].id

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)
            initPagination.setCurrentPage(1)

            GetAllUsersWithExcludeByIdsUseCase(initPagination, arrayListOf(excludeIdA)).execute().catch { error ->
                fail("expected: onCompletion, actual: catch, details: $error")
            }.onCompletion {
                completedLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                    loadedPagination = result.getOrThrow().second
                }
                if (result.isFailure) {
                    fail("expected: Result.Success, actual: Result.Failure")
                }
            }
        }

        val usersB = QBUsers.getUsers(makeRequestBuilder(2, 10)).perform()
        val excludeIdB = usersB[0].id

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)
            initPagination.setCurrentPage(2)

            GetAllUsersWithExcludeByIdsUseCase(initPagination, arrayListOf(excludeIdB)).execute().catch { error ->
                fail("expected: onCompletion, actual: catch, details: $error")
            }.onCompletion {
                completedLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                    loadedPagination = result.getOrThrow().second
                }
                if (result.isFailure) {
                    fail("expected: Result.Success, actual: Result.Failure")
                }
            }
        }

        assertEquals(0, completedLatch.count)

        assertEquals(18, loadedUsers.size)

        assertEquals(loadedPagination!!.getPerPage(), 10)
        assertEquals(loadedPagination!!.getCurrentPage(), 2)
        assertTrue(loadedPagination!!.hasNextPage())

        assertTrue(usersNotContainsExcludeIds(arrayListOf(excludeIdA, excludeIdB), loadedUsers))
    }

    private fun makeRequestBuilder(page: Int, perPage: Int): QBPagedRequestBuilder {
        val requestBuilder = QBPagedRequestBuilder()

        requestBuilder.page = page
        requestBuilder.perPage = perPage
        return requestBuilder
    }

    private fun usersNotContainsExcludeIds(excludeIds: Collection<Int>, users: Collection<UserEntity?>): Boolean {
        var notContains = true

        run breakLoop@{
            users.forEach { user ->
                if (excludeIds.contains(user?.getUserId())) {
                    notContains = false
                    return@breakLoop
                }
            }
        }

        return notContains
    }
}