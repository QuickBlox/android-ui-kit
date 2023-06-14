/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.repository.UsersRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.stub.entity.DialogEntityStub
import com.quickblox.android_ui_kit.stub.entity.UserEntityStub
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch

class GetUsersFromDialogUseCaseTest : BaseTest() {
    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun createDialogWithoutUsers_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        GetUsersFromDialogUseCase(buildDialogEntity(arrayListOf())).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogWithOneUser_execute_receivedOneUser() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                return buildUserEntity(777)
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val loadedUsers = mutableListOf<UserEntity>()
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(777))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                loadedUsers.addAll(result)
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception, details $error")
            }
        }

        assertEquals(1, loadedUsers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun createDialogOnlyWithLoggedUser_execute_receivedZeroUsers() = runTest {
        val loggedUserId = 888

        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                return buildUserEntity(777)
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

        val loadedUsers = mutableListOf<UserEntity>()
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(loggedUserId))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                loadedUsers.addAll(result)
            }.onFailure { error ->
                Assert.fail("expected: NoException, actual: Exception, details $error")
            }
        }

        assertEquals(1, loadedUsers.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getUserFromRemoteThrowUnauthorisedException_execute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                throw UsersRepositoryException(UsersRepositoryException.Codes.UNAUTHORISED, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(888))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getUserFromRemoteThrowIncorrectDataException_execute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                throw UsersRepositoryException(UsersRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(888))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getRemoteUserThrowUnauthorisedException_execute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                throw UsersRepositoryException(UsersRepositoryException.Codes.UNAUTHORISED, "")
            }

            override fun getLoggedUserId(): Int {
                return 777
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(888))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getRemoteUserThrowIncorrectDataException_execute_receivedException() = runTest {
        val usersRepository = object : UsersRepositorySpy() {
            override fun getUserFromRemote(userId: Int): UserEntity {
                throw UsersRepositoryException(UsersRepositoryException.Codes.INCORRECT_DATA, "")
            }

            override fun getLoggedUserId(): Int {
                return 777
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getUsersRepository(): UsersRepository {
                return usersRepository
            }
        })

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                val dialogEntity = buildDialogEntity(arrayListOf(888))
                GetUsersFromDialogUseCase(dialogEntity).execute()
            }.onSuccess { result ->
                Assert.fail("expected: Exception, actual: NotException")
            }.onFailure { error ->
                errorLatch.countDown()
            }
        }

        assertEquals(0, errorLatch.count)
    }

    private fun buildUserEntity(userId: Int): UserEntity {
        return object : UserEntityStub() {
            override fun getUserId(): Int {
                return userId
            }
        }
    }

    private fun buildDialogEntity(occupantIds: List<Int>): DialogEntity {
        return object : DialogEntityStub() {
            override fun getParticipantIds(): List<Int> {
                return occupantIds
            }
        }
    }
}