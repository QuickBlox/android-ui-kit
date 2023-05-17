/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.repository.user.UsersRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.stub.repository.UsersRepositoryStub
import com.quickblox.android_ui_kit.stub.source.LocalDataSourceStub
import com.quickblox.android_ui_kit.stub.source.RemoteDataSourceStub
import com.quickblox.core.exception.QBResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UserRepositoryTest : BaseTest() {
    private val localExceptionFactory = LocalDataSourceExceptionFactoryImpl()
    private val remoteExceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    @Test
    fun getUserFromRemote_NoException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun getUser(dto: RemoteUserDTO): RemoteUserDTO {
                val dtoStub = RemoteUserDTO()
                dtoStub.id = 12345
                return dtoStub
            }
        }

        val usersRepository = UsersRepositoryImpl(remoteDataSourceStub, object : LocalDataSourceStub() {})
        val entity = usersRepository.getUserFromRemote(123456)

        assertNotNull(entity)
    }

    @Test
    fun getUserFromRemote_ConnectionFailedException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun getUser(dto: RemoteUserDTO): RemoteUserDTO {
                val serviceUnavailableCode = 503
                val errors = arrayListOf<String>()
                errors.add("Service unavailable")

                val responseException = QBResponseException(serviceUnavailableCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val usersRepository = UsersRepositoryImpl(remoteDataSourceStub, object : LocalDataSourceStub() {})
        try {
            usersRepository.getUserFromRemote(123456)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: UsersRepositoryException) {
            assertEquals(UsersRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    fun existRemoteUsers_getAllUsersFromRemote_receiveUsers() = runTest {
        val usersRepository = object : UsersRepositoryStub() {
            override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
                return flow {
                    buildStubXUsers(3).forEach {
                        emit(Result.success(Pair(it, PaginationEntityImpl())))
                    }
                }
            }
        }

        val usersCount = usersRepository.getAllUsersFromRemote(PaginationEntityImpl()).count()
        assertEquals(3, usersCount)
    }
}