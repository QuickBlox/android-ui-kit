/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.entity.UserEntityStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

class GetAllUsersWithExcludeByIdsUseCaseTest : BaseTest() {
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
    fun availableUsers_execute_receivedUsers() = runTest {
        val loggedUserId = 888
        val returnedUsers = buildUsers(5)

        val usersRepository = object : UsersRepositorySpy() {
            override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
                return channelFlow {
                    returnedUsers.forEach { user ->
                        send(Result.success(Pair(user, PaginationEntityImpl())))
                    }
                }.buffer(1)
            }

            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val completionLatch = CountDownLatch(1)
        val loadedUsers = mutableListOf<UserEntity>()
        withContext(UnconfinedTestDispatcher()) {
            GetAllUsersWithExcludeByIdsUseCase(PaginationEntityImpl(), arrayListOf(777, 555)).execute()
                .onCompletion {
                    completionLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    loadedUsers.add(result.getOrThrow().first)
                }
        }

        assertEquals(0, completionLatch.count)
        assertEquals(returnedUsers.size, loadedUsers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun availableUsers_executeWithEmptyUserIds_receivedError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            GetAllUsersWithExcludeByIdsUseCase(PaginationEntityImpl(), arrayListOf()).execute()
                .catch { error ->
                    errorLatch.countDown()
                }.collect { result ->
                    Assert.fail("expected: Exception, actual: NoException")
                }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getUsersWithExcludeIdsThrowException_execute_receivedError() = runTest {
        val loggedUserId = 888
        val returnedUsers = buildUsers(5)

        val usersRepository = object : UsersRepositorySpy() {
            override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
                return channelFlow<Result<Pair<UserEntity, PaginationEntity>>> {
                    val exception = RemoteDataSourceException(RemoteDataSourceException.Codes.INCORRECT_DATA, "")
                    send(Result.failure(exception))
                }.buffer(1)
            }

            override fun getLoggedUserId(): Int {
                return loggedUserId
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            GetAllUsersWithExcludeByIdsUseCase(PaginationEntityImpl(), arrayListOf(777, 555)).execute()
                .catch { error ->
                    Assert.fail("expected: NoException, actual: Exception, details $error")
                }.collect { result ->
                    if (result.isFailure) {
                        errorLatch.countDown()
                    }
                }
        }

        assertEquals(0, errorLatch.count)
    }

    private fun buildUsers(countOfUsers: Int): List<UserEntity> {
        val loadedUsersDTO = arrayListOf<UserEntity>()

        repeat(countOfUsers) {
            val userEntity = buildUserEntity(Random.nextInt(1000, 5000))
            loadedUsersDTO.add(userEntity)
        }

        return loadedUsersDTO
    }

    private fun buildUserEntity(userId: Int): UserEntity {
        return object : UserEntityStub() {
            override fun getUserId(): Int {
                return userId
            }
        }
    }
}