/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class GetAllMessagesUseCase(
    private val dialog: DialogEntity,
    private val paginationEntity: PaginationEntity,
) : FlowUseCase<Result<Pair<MessageEntity?, PaginationEntity>>>() {
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()

    override suspend fun execute(): Flow<Result<Pair<MessageEntity?, PaginationEntity>>> {
        return channelFlow {
            if (dialog.getDialogId()?.isEmpty() == true) {
                throw DomainException("The dialogId should not be empty")
            }

            if (dialog.getParticipantIds()?.isEmpty() == true) {
                throw DomainException("The participantIds should not be empty")
            }

            withContext(Dispatchers.IO) {
                val usersFromDialog = loadUsersFrom(dialog)

                messagesRepository.getMessagesFromRemote(dialog.getDialogId()!!, paginationEntity).onEach { result ->
                    if (result.isSuccess) {
                        val message = result.getOrNull()?.first

                        val isIncomingChatMessage = message is IncomingChatMessageEntity?

                        if (isIncomingChatMessage) {
                            val incomingMessage = message as IncomingChatMessageEntity?
                            val senderId = incomingMessage?.getSenderId()

                            val user = getUser(senderId, usersFromDialog)
                            user?.let {
                                message?.setSender(user)
                            }
                        }

                        val pair = Pair(message, result.getOrNull()?.second!!)
                        send(Result.success(pair))
                    } else {
                        send(result)
                    }
                }.collect()
            }
        }.buffer(1)
    }

    private fun loadUsersFrom(dialogEntity: DialogEntity): Collection<UserEntity> {
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
}