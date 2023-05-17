/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.CustomDataEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreatePrivateDialogUseCase(private val participantId: Int, private val customData: CustomDataEntity? = null) :
    BaseUseCase<DialogEntity?>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val userRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): DialogEntity? {
        if (isNotCorrectParticipant(participantId)) {
            throw DomainException("The participant has incorrect value $participantId")
        }

        var createdDialog: DialogEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                createdDialog = dialogsRepository.createDialogInRemote(buildDialog())
                createdDialog?.let { dialog ->
                    dialogsRepository.saveDialogToLocal(dialog)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return createdDialog
    }

    private suspend fun isNotCorrectParticipant(participantId: Int): Boolean {
        if (participantId <= 0) {
            return true
        }

        var isNotCorrect: Boolean

        withContext(Dispatchers.IO) {
            isNotCorrect = userRepository.getLoggedUserId() == participantId
        }
        return isNotCorrect
    }

    private fun buildDialog(): DialogEntity {
        val dialogEntity: DialogEntity = DialogEntityImpl()
        dialogEntity.setCustomData(customData)
        dialogEntity.setParticipantIds(arrayListOf(participantId))
        dialogEntity.setDialogType(DialogEntity.Types.PRIVATE)

        return dialogEntity
    }
}