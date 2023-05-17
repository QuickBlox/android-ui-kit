/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.DEFAULT_DELAY
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.user.UsersRepositoryExceptionFactoryImpl
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import java.util.concurrent.TimeUnit

class LoadUsersByNameUseCaseTest : BaseTest() {
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
    fun userRepositoryHasUserEntity_execute_receiveUsers() = runTest {
        val remoteUsersCount = 5

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return UsersRepositorySpy(countOfRemoteUsers = remoteUsersCount)
            }
        })

        val loadedUsers = mutableListOf<UserEntity?>()

        val completedLatch = CountDownLatch(1)
        LoadUsersByNameUseCase(PaginationEntityImpl(), "test").execute().catch {
            fail("expected: onCompletion, actual: catch")
        }.onCompletion {
            completedLatch.countDown()
        }.collect { result ->
            if (result.isSuccess) {
                loadedUsers.add(result.getOrThrow().first)
            }
        }

        completedLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(completedLatch.count, 0)
        assertTrue(loadedUsers.count() == remoteUsersCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getAllUsersFromRemoteThrowException_execute_Exception() = runTest {
        val userRepository = object : UsersRepositorySpy() {
            override fun getUsersByNameFromRemote(
                paginationEntity: PaginationEntity,
                name: String
            ): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
                throw UsersRepositoryExceptionFactoryImpl().makeIncorrectData("")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return userRepository
            }
        })

        val completedLatch = CountDownLatch(2)

        LoadUsersByNameUseCase(PaginationEntityImpl(), "test").execute().catch {
            completedLatch.countDown()
        }.onCompletion {
            completedLatch.countDown()
        }.collect { result ->
            fail("expected: onCompletion, actual: catch")
        }

        completedLatch.await(DEFAULT_DELAY, TimeUnit.SECONDS)

        assertEquals(completedLatch.count, 0)
    }
}