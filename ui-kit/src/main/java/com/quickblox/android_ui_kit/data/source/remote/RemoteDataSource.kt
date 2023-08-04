/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessagePaginationDTO
import com.quickblox.android_ui_kit.data.dto.remote.typing.RemoteTypingDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserFilterDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    @Throws(RemoteDataSourceException::class)
    fun connect()

    @Throws(RemoteDataSourceException::class)
    fun disconnect()

    @Throws(RemoteDataSourceException::class)
    fun subscribeConnection(): Flow<Boolean>

    @Throws(RemoteDataSourceException::class)
    fun subscribeDialogsEvent(): Flow<RemoteDialogDTO?>

    @Throws(RemoteDataSourceException::class)
    fun subscribeMessagesEvent(): Flow<RemoteMessageDTO?>

    @Throws(RemoteDataSourceException::class)
    fun subscribeTypingEvent(): Flow<RemoteTypingDTO?>

    @Throws(RemoteDataSourceException::class)
    fun startTyping(dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun stopTyping(dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO

    @Throws(RemoteDataSourceException::class)
    fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO

    @Throws(RemoteDataSourceException::class)
    fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO

    @Throws(RemoteDataSourceException::class)
    fun getAllDialogs(): Flow<Result<RemoteDialogDTO>>

    @Throws(RemoteDataSourceException::class)
    fun leaveDialog(dto: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun getLoggedUserId(): Int

    @Throws(RemoteDataSourceException::class)
    fun getUserSessionToken(): String

    @Throws(RemoteDataSourceException::class)
    fun getUser(dto: RemoteUserDTO): RemoteUserDTO

    @Throws(RemoteDataSourceException::class)
    fun getUsers(userIds: Collection<Int>): Collection<RemoteUserDTO>

    @Throws(RemoteDataSourceException::class)
    fun getAllUsers(dto: RemoteUserPaginationDTO): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>>

    @Throws(RemoteDataSourceException::class)
    fun addUsersToDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO

    @Throws(RemoteDataSourceException::class)
    fun removeUsersFromDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO

    @Throws(RemoteDataSourceException::class)
    fun getUsersByFilter(
        paginationDTO: RemoteUserPaginationDTO,
        filterDto: RemoteUserFilterDTO
    ): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>>

    @Throws(RemoteDataSourceException::class)
    fun getAllMessages(
        messageDTO: RemoteMessageDTO,
        paginationDTO: RemoteMessagePaginationDTO
    ): Flow<Result<Pair<RemoteMessageDTO?, RemoteMessagePaginationDTO>>>

    @Throws(RemoteDataSourceException::class)
    fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun sendEventMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun updateMessage(dto: RemoteMessageDTO)

    @Throws(RemoteDataSourceException::class)
    fun createMessage(dto: RemoteMessageDTO): RemoteMessageDTO

    @Throws(RemoteDataSourceException::class)
    fun readMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun deliverMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO)

    @Throws(RemoteDataSourceException::class)
    fun deleteMessage(dto: RemoteMessageDTO)

    @Throws(RemoteDataSourceException::class)
    fun createFile(dto: RemoteFileDTO): RemoteFileDTO

    @Throws(RemoteDataSourceException::class)
    fun getFile(dto: RemoteFileDTO): RemoteFileDTO

    @Throws(RemoteDataSourceException::class)
    fun deleteFile(dto: RemoteFileDTO)
}