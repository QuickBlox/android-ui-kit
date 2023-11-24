/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendForwardMessageUseCase(
    private val forwardMessage: OutgoingChatMessageEntity, private val dialogIdToForward: String
) : BaseUseCase<Unit>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute() {
        if (dialogIdToForward.isEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val localDialog = dialogsRepository.getDialogFromLocal(dialogIdToForward)

                sendMessage(forwardMessage, localDialog)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    @VisibleForTesting
    fun sendMessage(message: OutgoingChatMessageEntity?, dialog: DialogEntity) {
        message?.let {
            messagesRepository.sendChatMessageToRemote(message, dialog)
        }
    }
}