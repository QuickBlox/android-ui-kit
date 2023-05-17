/*
 * Created by Injoit on 24.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext

class LoadUsersUseCase(private val pagination: PaginationEntity) :
    FlowUseCase<Result<Pair<UserEntity, PaginationEntity>>>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            withContext(Dispatchers.IO) {
                usersRepository.getAllUsersFromRemote(pagination).collect { result ->
                    withContext(Dispatchers.Main) {
                        send(result)
                    }
                }
            }
        }
    }
}