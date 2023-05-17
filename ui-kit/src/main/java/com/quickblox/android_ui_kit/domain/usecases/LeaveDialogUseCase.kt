/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.message.EventMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LeaveDialogUseCase(private val dialogEntity: DialogEntity) : BaseUseCase<Unit>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute() {
        val dialogId = dialogEntity.getDialogId()
        if (dialogId.isNullOrEmpty()) {
            throw DomainException("dialogId shouldn't be null or empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val isGroupDialog = dialogEntity.getType() == DialogEntity.Types.GROUP
                if (isGroupDialog) {
                    sendEvent(dialogEntity)
                }

                dialogsRepository.leaveDialogFromRemote(dialogEntity)

                dialogsRepository.deleteDialogFromLocal(dialogId)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    private fun sendEvent(dialog: DialogEntity) {
        val loggedUserName = getLoggedUserName()
        val messageText = createMessageText(loggedUserName)
        val event = createEvent(messageText, dialog.getDialogId()!!)

        messagesRepository.sendEventMessageToRemote(event, dialog)
    }

    @VisibleForTesting
    fun createMessageText(userName: String): String {
        val messageText = "User $userName left"
        return messageText
    }

    private fun getLoggedUserName(): String {
        val loggedUser = getLoggedUser()
        return getUserNameFrom(loggedUser)
    }

    @VisibleForTesting
    fun getUserNameFrom(user: UserEntity): String {
        var userName = ""
        if (user.getName()?.isNotBlank() == true) {
            userName = user.getName() ?: ""
        }
        if (userName.isBlank()) {
            userName = user.getLogin() ?: ""
        }
        return userName
    }

    private fun getLoggedUser(): UserEntity {
        val loggedUserId = usersRepository.getLoggedUserId()
        return usersRepository.getUserFromRemote(loggedUserId)
    }

    @VisibleForTesting
    fun createEvent(text: String, dialogId: String): EventMessageEntity {
        val eventMessageEntity = EventMessageEntityImpl()
        eventMessageEntity.setText(text)
        eventMessageEntity.setEventType(EventMessageEntity.EventTypes.LEFT_USER_FROM_DIALOG)
        eventMessageEntity.setDialogId(dialogId)
        eventMessageEntity.setTime(System.currentTimeMillis() / 1000)

        return eventMessageEntity
    }
}