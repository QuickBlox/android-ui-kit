/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadMessageUseCase(private val message: IncomingChatMessageEntity) : BaseUseCase<Unit>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    override suspend fun execute() {
        if (message.getDialogId().isNullOrEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        if (isWrongSender(message.getSenderId())) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val dialog = getDialogBy(message.getDialogId()!!)
                messagesRepository.readMessage(message, dialog)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    private fun isWrongSender(participant: Int?): Boolean {
        return participant == null || participant <= 0
    }

    private fun getDialogBy(dialogId: String): DialogEntity {
        return dialogsRepository.getDialogFromLocal(dialogId)
    }
}