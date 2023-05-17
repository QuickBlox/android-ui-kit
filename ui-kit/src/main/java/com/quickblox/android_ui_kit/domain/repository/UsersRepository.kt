/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    @Throws(UsersRepositoryException::class)
    fun getUserFromRemote(userId: Int): UserEntity

    @Throws(UsersRepositoryException::class)
    fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity>

    @Throws(UsersRepositoryException::class)
    fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>>

    @Throws(UsersRepositoryException::class)
    fun getUsersByNameFromRemote(
        paginationEntity: PaginationEntity,
        name: String
    ): Flow<Result<Pair<UserEntity, PaginationEntity>>>

    @Throws(UsersRepositoryException::class)
    fun getLoggedUserId(): Int
}