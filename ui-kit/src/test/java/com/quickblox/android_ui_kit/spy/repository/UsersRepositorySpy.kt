/*
 * Created by Injoit on 7.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.spy.entity.UserEntitySpy
import com.quickblox.android_ui_kit.stub.entity.UserEntityStub
import com.quickblox.android_ui_kit.stub.repository.UsersRepositoryStub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlin.random.Random

open class UsersRepositorySpy(val countOfRemoteUsers: Int = 10) : UsersRepositoryStub() {
    override fun getUserFromRemote(userId: Int): UserEntity {
        return UserEntitySpy()
    }

    override fun getUsersFromRemote(userIds: Collection<Int>): Collection<UserEntity> {
        val users = arrayListOf<UserEntity>()

        userIds.forEach { userId ->
            users.add(buildStubUserWithName("user_name: ${System.currentTimeMillis()}"))
        }

        return users
    }

    override fun getAllUsersFromRemote(paginationEntity: PaginationEntity): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            buildStubXUsers(countOfRemoteUsers).forEach { user ->
                send(Result.success(Pair(user, PaginationEntityImpl())))
            }
        }.buffer(1)
    }

    override fun getUsersByNameFromRemote(
        paginationEntity: PaginationEntity,
        name: String
    ): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            buildStubXUsers(countOfRemoteUsers).forEach { user ->
                send(Result.success(Pair(user, PaginationEntityImpl())))
            }
        }.buffer(1)
    }

    override fun getLoggedUserId(): Int {
        return 77777777
    }

    private fun buildStubXUsers(usersCount: Int): List<UserEntity> {
        val dialogs = mutableListOf<UserEntity>()
        for (index in 1..usersCount) {
            if (index % 2 == 0) {
                dialogs.add(buildStubUserWithName("user_name: ${System.currentTimeMillis()}"))
            } else {
                dialogs.add(buildStubUserWithLogin("user_name: ${System.currentTimeMillis()}"))
            }
        }
        return dialogs
    }

    fun buildStubUserWithName(name: String): UserEntity {
        return object : UserEntityStub() {
            override fun getName(): String? {
                return name
            }

            override fun getUserId(): Int {
                return Random.nextInt(3000, 5000)
            }
        }
    }

    fun buildStubUserWithLogin(login: String): UserEntity {
        return object : UserEntityStub() {
            override fun getName(): String? {
                return ""
            }

            override fun getLogin(): String? {
                return login
            }

            override fun getUserId(): Int {
                return Random.nextInt(3000, 5000)
            }
        }
    }

    private fun buildStubUser(): UserEntity {
        return object : UserEntityStub() {
            override fun getUserId(): Int {
                return Random.nextInt(3000, 5000)
            }
        }
    }
}