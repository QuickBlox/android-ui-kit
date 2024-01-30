/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadMessageUseCase(private val message: MessageEntity) : BaseUseCase<Unit>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute() {
        if (message.getDialogId().isNullOrEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        if (isWrongSenderId(message.getSenderId())) {
            throw DomainException("The senderId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val dialog = getDialogAndIfNeedUpdateInLocal(message.getDialogId()!!)

                messagesRepository.readMessage(message, dialog)

                if (message is IncomingChatMessageEntity) {
                    setReadIdsToIncomingMessage(message)
                }

                if (message is EventMessageEntity) {
                    setReadIdsToEventMessage(message)
                }

                val dialogWithUpdatedUnreadMessageCount = updateUnreadMessageCountIn(dialog)
                updateDialogInLocal(dialogWithUpdatedUnreadMessageCount)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    // TODO: Need to add one logic to add readId to IncomingChatMessageEntity and EventMessageEntity
    private fun setReadIdsToIncomingMessage(message: IncomingChatMessageEntity) {
        val loggedUserId = getLoggedUserId()
        val reads = message.getReadIds()?.toMutableList()
        reads?.add(loggedUserId)
        message.setReadIds(reads)
    }

    // TODO: Need to add one logic to add readId to IncomingChatMessageEntity and EventMessageEntity
    private fun setReadIdsToEventMessage(message: EventMessageEntity) {
        val loggedUserId = getLoggedUserId()
        val reads = message.getReadIds()?.toMutableList()
        reads?.add(loggedUserId)
        message.setReadIds(reads)
    }

    private suspend fun getDialogAndIfNeedUpdateInLocal(dialogId: String): DialogEntity {
        var foundDialog: DialogEntity? = null

        runCatching {
            foundDialog = getDialogFromLocal(dialogId)

        }.onFailure { error ->
            foundDialog = getDialogFromRemote(dialogId)

            if (foundDialog == null) {
                throw DomainException("Dialog not found")
            } else {
                updateDialogInLocal(foundDialog!!)
            }
        }
        return foundDialog!!
    }

    @VisibleForTesting
    fun isWrongSenderId(senderId: Int?): Boolean {
        return senderId == null || senderId <= 0
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

    @VisibleForTesting
    fun getDialogFromLocal(dialogId: String): DialogEntity? {
        var foundDialog: DialogEntity? = null

        runCatching {
            foundDialog = dialogsRepository.getDialogFromLocal(dialogId)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }

        return foundDialog
    }

    @VisibleForTesting
    fun getDialogFromRemote(dialogId: String): DialogEntity? {
        var foundDialog: DialogEntity? = null

        runCatching {
            foundDialog = dialogsRepository.getDialogFromRemote(dialogId)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }

        return foundDialog
    }

    @VisibleForTesting
    suspend fun updateDialogInLocal(dialog: DialogEntity) {
        runCatching {
            dialogsRepository.updateDialogInLocal(dialog)
        }.onFailure { error ->
            throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
        }
    }

    private fun getLoggedUserId(): Int {
        return usersRepository.getLoggedUserId()
    }
}