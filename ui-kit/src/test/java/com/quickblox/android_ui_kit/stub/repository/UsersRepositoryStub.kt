/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.stub.repository

import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.repository.UsersRepository
import com.quickblox.android_ui_kit.stub.BaseStub
import kotlinx.coroutines.flow.Flow

open class UsersRepositoryStub : BaseStub(), UsersRepository {
    override fun getUserFromRemote(userId: Int): UserEntity {
        throw buildRuntimeException()
    }

    override fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity> {
        throw buildRuntimeException()
    }

    override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        throw buildRuntimeException()
    }

    override fun getUsersByNameFromRemote(
        paginationEntity: PaginationEntity,
        name: String
    ): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        throw buildRuntimeException()
    }

    override fun getLoggedUserId(): Int {
        throw buildRuntimeException()
    }
}