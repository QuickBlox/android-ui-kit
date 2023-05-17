/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.user.UsersRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.domain.usecases.LoadUsersUseCase
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
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LoadUsersUseCasePositiveTest : BaseTest() {
    private val userRepository: UsersRepository = UsersRepositoryImpl(RemoteDataSourceImpl(), LocalDataSourceImpl())

    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
    }

    private fun initDependency() {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return userRepository
            }
        })
    }

    @After
    fun release() {
        logoutFromRest()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun paginationChangeToLoad10Users_execute_loaded10Users() = runBlocking {
        val completedLatch = CountDownLatch(1)

        val loadedUsers = mutableListOf<UserEntity?>()
        var loadedPagination: PaginationEntity? = null

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)

            LoadUsersUseCase(initPagination).execute().catch {
                fail("expected: onCompletion, actual: catch")
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

        completedLatch.await(20, TimeUnit.SECONDS)

        assertEquals(completedLatch.count, 0)

        assertEquals(loadedUsers.size, 10)

        assertEquals(loadedPagination!!.getPerPage(), 10)
        assertEquals(loadedPagination!!.getCurrentPage(), 1)
        assertTrue(loadedPagination!!.hasNextPage())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun paginationChangeToLoad10Users_executeTwice_loaded20Users() = runBlocking {
        val completedLatch = CountDownLatch(2)

        val loadedUsers = mutableListOf<UserEntity?>()
        var loadedPagination: PaginationEntity? = null

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)
            initPagination.setCurrentPage(1)

            LoadUsersUseCase(initPagination).execute().catch {
                fail("expected: onCompletion, actual: catch")
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

        withContext(Dispatchers.Main) {
            val initPagination = PaginationEntityImpl()
            initPagination.setPerPage(10)
            initPagination.setCurrentPage(2)

            LoadUsersUseCase(initPagination).execute().catch {
                fail("expected: onCompletion, actual: catch")
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

        completedLatch.await(20, TimeUnit.SECONDS)

        assertEquals(completedLatch.count, 0)

        assertEquals(loadedUsers.size, 20)

        assertEquals(loadedPagination!!.getPerPage(), 10)
        assertEquals(loadedPagination!!.getCurrentPage(), 2)
        assertTrue(loadedPagination!!.hasNextPage())
    }
}