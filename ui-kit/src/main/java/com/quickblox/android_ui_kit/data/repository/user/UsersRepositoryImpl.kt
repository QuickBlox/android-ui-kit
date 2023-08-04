/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.user

import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserFilterDTO
import com.quickblox.android_ui_kit.data.repository.mapper.UserMapper
import com.quickblox.android_ui_kit.data.repository.mapper.UserPaginationMapper
import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class UsersRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : UsersRepository {
    private val exceptionFactory: UsersRepositoryExceptionFactory = UsersRepositoryExceptionFactoryImpl()

    override fun getUserFromRemote(userId: Int): UserEntity {
        try {
            val dto = UserMapper.remoteDTOFrom(userId)
            val remoteDTO = remoteDataSource.getUser(dto)
            return UserMapper.toEntity(remoteDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity> {
        try {
            val remoteDTOs = remoteDataSource.getUsers(userIds)
            return UserMapper.toEntity(remoteDTOs)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            val dto = UserPaginationMapper.dtoFrom(paginationEntity)
            remoteDataSource.getAllUsers(dto).collect { result ->
                if (result.isSuccess) {
                    try {
                        val userDTO = result.getOrThrow().first
                        val paginationDTO = result.getOrThrow().second

                        val mappedUserEntity = UserMapper.toEntity(userDTO)
                        val mappedPaginationEntity = UserPaginationMapper.entityFrom(paginationDTO)

                        send(Result.success(Pair(mappedUserEntity, mappedPaginationEntity)))
                    } catch (exception: MappingException) {
                        send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString())))
                    }
                }
                if (result.isFailure) {
                    val exception = result.getOrThrow() as Exception
                    send(Result.failure(exception))
                }
            }
        }
    }

    override fun getUsersByNameFromRemote(
        paginationEntity: PaginationEntity,
        name: String
    ): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            val paginationDTO = UserPaginationMapper.dtoFrom(paginationEntity)

            val filterDTO = RemoteUserFilterDTO()
            filterDTO.name = name

            remoteDataSource.getUsersByFilter(paginationDTO, filterDTO).collect { result ->
                if (result.isSuccess) {
                    try {
                        val userDTO = result.getOrThrow().first
                        val paginationDTO = result.getOrThrow().second

                        val mappedUserEntity = UserMapper.toEntity(userDTO)
                        val mappedPaginationEntity = UserPaginationMapper.entityFrom(paginationDTO)

                        send(Result.success(Pair(mappedUserEntity, mappedPaginationEntity)))
                    } catch (exception: MappingException) {
                        send(Result.failure(exceptionFactory.makeIncorrectData(exception.message.toString())))
                    }
                }
                if (result.isFailure) {
                    val exception = result.getOrThrow() as Exception
                    send(Result.failure(exception))
                }
            }
        }
    }

    override fun getLoggedUserId(): Int {
        try {
            return remoteDataSource.getLoggedUserId()
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getUserSessionToken(): String {
        try {
            return remoteDataSource.getUserSessionToken()
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }
}