/*
 * Created by Injoit on 8.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GetDialogByIdUseCase(private val dialogId: String) : BaseUseCase<DialogEntity?>() {
    private val dialogRepository = QuickBloxUiKit.getDependency().getDialogsRepository()

    override suspend fun execute(): DialogEntity? {
        if (dialogId.isEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        var foundDialog: DialogEntity?

        withContext(Dispatchers.IO) {
            val syncedLocalCache = dialogRepository.subscribeLocalSyncing().first()
            if (syncedLocalCache) {
                foundDialog = getDialogFromLocal()
            } else {
                foundDialog = getDialogFromRemoteAndUpdateInLocal()
            }
        }

        return foundDialog
    }

    private fun getDialogFromLocal(): DialogEntity? {
        var foundDialog: DialogEntity? = null

        runCatching {
            foundDialog = dialogRepository.getDialogFromLocal(dialogId)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }

        return foundDialog
    }

    private suspend fun getDialogFromRemoteAndUpdateInLocal(): DialogEntity? {
        val dialogFromRemote = getDialogFromRemote()
        dialogFromRemote?.let {
            updateDialogInLocal(dialogFromRemote)
        }
        return dialogFromRemote
    }

    private fun getDialogFromRemote(): DialogEntity? {
        var foundDialog: DialogEntity? = null

        runCatching {
            foundDialog = dialogRepository.getDialogFromRemote(dialogId)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }

        return foundDialog
    }

    private suspend fun updateDialogInLocal(dialog: DialogEntity) {
        runCatching {
            dialogRepository.updateDialogInLocal(dialog)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }
    }
}