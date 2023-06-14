/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadMessageUseCase(private val message: MessageEntity) : BaseUseCase<Unit>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    override suspend fun execute() {
        if (message.getDialogId().isNullOrEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        if (isWrongSenderId(message.getSenderId())) {
            throw DomainException("The senderId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val dialog = getDialogFromCache(message.getDialogId()!!)
                messagesRepository.readMessage(message, dialog)

                val dialogWithUpdatedUnreadMessageCount = updateUnreadMessageCountIn(dialog)
                updateDialogInCache(dialogWithUpdatedUnreadMessageCount)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    @VisibleForTesting
    fun isWrongSenderId(senderId: Int?): Boolean {
        return senderId == null || senderId <= 0
    }

    private fun getDialogFromCache(dialogId: String): DialogEntity {
        return dialogsRepository.getDialogFromLocal(dialogId)
    }

    @VisibleForTesting
    fun updateUnreadMessageCountIn(dialog: DialogEntity): DialogEntity {
        val oldUnreadMessageCount = dialog.getUnreadMessagesCount()
        val updatedUnreadMessageCount = updateUnreadMessageCount(oldUnreadMessageCount)
        dialog.setUnreadMessagesCount(updatedUnreadMessageCount)

        return dialog
    }

    @VisibleForTesting
    fun updateUnreadMessageCount(oldCount: Int?): Int? {
        if (oldCount == null) {
            return oldCount
        }

        val updatedUnreadMessageCount = oldCount - 1
        if (updatedUnreadMessageCount < 0) {
            return 0
        }

        return updatedUnreadMessageCount
    }

    private suspend fun updateDialogInCache(dialog: DialogEntity) {
        dialogsRepository.updateDialogInLocal(dialog)
    }
}