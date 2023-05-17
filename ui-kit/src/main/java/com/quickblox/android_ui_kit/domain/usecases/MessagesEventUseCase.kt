/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MessagesEventUseCase(private val dialog: DialogEntity) : FlowUseCase<MessageEntity?>() {
    private var eventsRepository = QuickBloxUiKit.getDependency().getEventsRepository()
    private var usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    private var scope = CoroutineScope(Dispatchers.IO)
    private val messagesEventFlow = MutableSharedFlow<MessageEntity?>(0)

    override suspend fun execute(): MutableSharedFlow<MessageEntity?> {
        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        if (dialog.getDialogId()?.isEmpty() == true) {
            throw DomainException("The dialogId should not be empty")
        }

        if (dialog.getParticipantIds()?.isEmpty() == true) {
            throw DomainException("The participantIds should not be empty")
        }

        scope.launch {
            val usersFromDialog = getUsersFrom(dialog)

            eventsRepository.subscribeMessageEvents().onEach { messageEntity ->
                val isMessageFromSubscribedDialog = messageEntity?.getDialogId() == dialog.getDialogId()
                if (isMessageFromSubscribedDialog) {
                    val isIncomingMessage = messageEntity is IncomingChatMessageEntity
                    if (isIncomingMessage) {
                        addUserToMessage(messageEntity, usersFromDialog)
                    }

                    messageEntity?.let {
                        messagesEventFlow.emit(messageEntity)
                    }
                }
            }.collect()
        }
        return messagesEventFlow
    }

    private fun addUserToMessage(
        messageEntity: MessageEntity?, usersFromDialog: Collection<UserEntity>
    ): IncomingChatMessageEntity? {
        val incomingMessage = messageEntity as IncomingChatMessageEntity?
        val senderId = incomingMessage?.getSenderId()

        val user = getUser(senderId, usersFromDialog)
        user?.let {
            messageEntity?.setSender(user)
        }
        return incomingMessage
    }

    private fun getUsersFrom(dialogEntity: DialogEntity): Collection<UserEntity> {
        val users = arrayListOf<UserEntity>()

        val opponentIds = dialogEntity.getParticipantIds()
        opponentIds?.let {
            val loadedUser = usersRepository.getUsersFromRemote(opponentIds)
            users.addAll(loadedUser)
        }

        return users
    }

    private fun getUser(senderId: Int?, usersFromDialog: Collection<UserEntity>): UserEntity? {
        var foundUser: UserEntity? = null

        senderId?.let {
            foundUser = usersFromDialog.find { user ->
                user.getUserId() == senderId
            }

            if (foundUser == null) {
                foundUser = getUserFromRemote(senderId)
            }
        }

        return foundUser
    }

    private fun getUserFromRemote(userId: Int): UserEntity {
        return usersRepository.getUserFromRemote(userId)
    }

    override suspend fun release() {
        scope.cancel()
    }
}