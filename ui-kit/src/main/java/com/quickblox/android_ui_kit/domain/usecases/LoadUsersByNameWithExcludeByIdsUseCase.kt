/*
 * Created by Injoit on 24.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class LoadUsersByNameWithExcludeByIdsUseCase(
    private val pagination: PaginationEntity,
    private val name: String,
    private val excludeIds: Collection<Int>
) :
    FlowUseCase<Result<Pair<UserEntity, PaginationEntity>>>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): Flow<Result<Pair<UserEntity, PaginationEntity>>> {
        return channelFlow {
            if (excludeIds.isEmpty()) {
                throw DomainException("The excludeIds should have at least one id")
            }

            withContext(Dispatchers.IO) {
                usersRepository.getUsersByNameFromRemote(pagination, name).onEach { result ->
                    if (result.isSuccess) {
                        val user = result.getOrNull()?.first
                        val isUserNotExistInExclude = !excludeIds.contains(user?.getUserId())
                        if (isUserNotExistInExclude) {
                            send(result)
                        }
                    } else {
                        send(result)
                    }
                }.collect()
            }
        }
    }
}