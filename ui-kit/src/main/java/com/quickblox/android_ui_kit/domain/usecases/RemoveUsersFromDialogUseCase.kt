/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
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

class RemoveUsersFromDialogUseCase(private val dialogEntity: DialogEntity, private val userIds: Collection<Int>) :
    BaseUseCase<DialogEntity?>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): DialogEntity? {
        if (userIds.isEmpty()) {
            throw DomainException("The participants should not be NULL or empty")
        }

        if (isUsersContainsLoggedUser(userIds)) {
            throw DomainException("The participants should not contain logged user id")
        }

        if (dialogEntity.getType() == DialogEntity.Types.PRIVATE) {
            throw DomainException("You can't remove users from private dialog")
        }

        var updatedDialog: DialogEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                updatedDialog = dialogsRepository.removeUsersFromDialog(dialogEntity, userIds)

                sendEvent(updatedDialog!!)

                updatedDialog?.let { dialog ->
                    dialogsRepository.updateDialogInLocal(dialog)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return updatedDialog
    }

    private suspend fun isUsersContainsLoggedUser(userIds: Collection<Int>): Boolean {
        var contains = false

        withContext(Dispatchers.IO) {
            val loggedUserId = usersRepository.getLoggedUserId()

            run breakLoop@{
                for (userId in userIds) {
                    if (userId == loggedUserId) {
                        contains = true
                        return@breakLoop
                    }
                }
            }
        }

        return contains
    }

    private fun sendEvent(dialog: DialogEntity) {
        val ownerName = getLoggedUserName()
        val allUserNames = getAllUserNamesFrom(userIds)
        val messageText = createMessageText(ownerName, allUserNames)
        val event = createEvent(messageText, dialog.getDialogId()!!)

        messagesRepository.sendEventMessageToRemote(event, dialog)
    }

    @VisibleForTesting
    fun createMessageText(ownerName: String, userNames: String): String {
        val userNamesSize = userNames.split(",").size

        var userPrefix = "user"
        if (userNamesSize > 1) {
            userPrefix = "users"
        }

        val messageText = "User $ownerName removed $userPrefix $userNames"
        return messageText
    }

    @VisibleForTesting
    fun getAllUserNamesFrom(userIds: Collection<Int>): String {
        var allUserNames = ""

        val users = usersRepository.getUsersFromRemote(userIds)
        for (user in users) {
            val userName = getUserNameFrom(user)
            allUserNames += "$userName, "
        }

        val cleanedUserNames = allUserNames.trim().substring(0, allUserNames.length - 2)

        return cleanedUserNames
    }

    private fun getLoggedUserName(): String {
        val loggedUser = getLoggedUser()
        return getUserNameFrom(loggedUser)
    }

    private fun getLoggedUser(): UserEntity {
        val loggedUserId = usersRepository.getLoggedUserId()
        return usersRepository.getUserFromRemote(loggedUserId)
    }

    @VisibleForTesting
    fun getUserNameFrom(user: UserEntity): String {
        var userName = ""
        if (user.getName()?.isNotBlank() == true) {
            userName = user.getName()!!
        }
        if (userName.isBlank()) {
            userName = user.getLogin() ?: ""
        }
        return userName
    }

    @VisibleForTesting
    fun createEvent(text: String, dialogId: String): EventMessageEntity {
        val eventMessageEntity = EventMessageEntityImpl()
        eventMessageEntity.setText(text)
        eventMessageEntity.setEventType(EventMessageEntity.EventTypes.REMOVED_USER_FROM_DIALOG)
        eventMessageEntity.setDialogId(dialogId)
        eventMessageEntity.setTime(System.currentTimeMillis() / 1000)

        return eventMessageEntity
    }
}