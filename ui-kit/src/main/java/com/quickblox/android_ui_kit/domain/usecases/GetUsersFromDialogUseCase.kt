/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetUsersFromDialogUseCase(private val dialogEntity: DialogEntity) : BaseUseCase<List<UserEntity>>() {
    private val userRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): List<UserEntity> {
        if (dialogEntity.getParticipantIds()?.isEmpty() == true) {
            throw DomainException("The participants count should be positive")
        }

        val users = mutableListOf<UserEntity>()

        withContext(Dispatchers.IO) {
            runCatching {
                dialogEntity.getParticipantIds()?.forEach { userId ->
                    val userEntity = userRepository.getUserFromRemote(userId)
                    users.add(userEntity)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return users
    }
}