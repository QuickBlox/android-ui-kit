/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateDialogUseCase(private val dialogEntity: DialogEntity) :
    BaseUseCase<DialogEntity?>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val userRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): DialogEntity? {
        if (isOwnerIdIncorrect(dialogEntity.getOwnerId())) {
            throw DomainException("The ownerId in dialog can't be null")
        }

        if (isLoggedUserIsNotOwner(dialogEntity.getOwnerId())) {
            throw DomainException("The logged user id is not owner for dialog ${dialogEntity.getDialogId()}")
        }

        var createdDialog: DialogEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                createdDialog = dialogsRepository.updateDialogInRemote(dialogEntity)
                createdDialog?.let { dialog ->
                    dialogsRepository.updateDialogInLocal(dialog)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return createdDialog
    }

    private fun isOwnerIdIncorrect(ownerId: Int?): Boolean {
        return ownerId == null || ownerId <= 0
    }

    private suspend fun isLoggedUserIsNotOwner(ownerId: Int?): Boolean {
        var isNotCorrect: Boolean

        withContext(Dispatchers.IO) {
            isNotCorrect = userRepository.getLoggedUserId() != ownerId
        }
        return isNotCorrect
    }
}