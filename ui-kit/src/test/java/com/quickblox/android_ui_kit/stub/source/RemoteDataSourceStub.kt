/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.source

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessagePaginationDTO
import com.quickblox.android_ui_kit.data.dto.remote.typing.RemoteTypingDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserFilterDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.stub.BaseStub
import kotlinx.coroutines.flow.Flow

open class RemoteDataSourceStub : BaseStub(), RemoteDataSource {
    override fun connect() {
        throw buildRuntimeException()
    }

    override fun disconnect() {
        throw buildRuntimeException()
    }

    override fun subscribeConnection(): Flow<Boolean> {
        throw buildRuntimeException()
    }

    override fun subscribeDialogsEvent(): Flow<RemoteDialogDTO?> {
        throw buildRuntimeException()
    }

    override fun subscribeMessagesEvent(): Flow<RemoteMessageDTO?> {
        throw buildRuntimeException()
    }

    override fun subscribeTypingEvent(): Flow<RemoteTypingDTO?> {
        throw buildRuntimeException()
    }

    override fun startTyping(dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun stopTyping(dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        throw buildRuntimeException()
    }

    override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        throw buildRuntimeException()
    }

    override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
        throw buildRuntimeException()
    }

    override fun getAllDialogs(): Flow<Result<RemoteDialogDTO>> {
        throw buildRuntimeException()
    }

    override fun leaveDialog(dto: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun getLoggedUserId(): Int {
        throw buildRuntimeException()
    }

    override fun getUserSessionToken(): String {
        throw buildRuntimeException()
    }

    override fun getUser(dto: RemoteUserDTO): RemoteUserDTO {
        throw buildRuntimeException()
    }

    override fun getUsers(userIds: Collection<Int>): Collection<RemoteUserDTO> {
        throw buildRuntimeException()
    }

    override fun getAllUsers(dto: RemoteUserPaginationDTO): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>> {
        throw buildRuntimeException()
    }

    override fun addUsersToDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO {
        throw buildRuntimeException()
    }

    override fun removeUsersFromDialog(dto: RemoteDialogDTO, userIds: Collection<Int>): RemoteDialogDTO {
        throw buildRuntimeException()
    }

    override fun getUsersByFilter(
        paginationDTO: RemoteUserPaginationDTO,
        filterDto: RemoteUserFilterDTO
    ): Flow<Result<Pair<RemoteUserDTO, RemoteUserPaginationDTO>>> {
        throw buildRuntimeException()
    }

    override fun getAllMessages(
        messageDTO: RemoteMessageDTO,
        paginationDTO: RemoteMessagePaginationDTO
    ): Flow<Result<Pair<RemoteMessageDTO, RemoteMessagePaginationDTO>>> {
        throw buildRuntimeException()
    }

    override fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun sendEventMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun updateMessage(dto: RemoteMessageDTO) {
        throw buildRuntimeException()
    }

    override fun createMessage(dto: RemoteMessageDTO): RemoteMessageDTO {
        throw buildRuntimeException()
    }

    override fun readMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun deliverMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
        throw buildRuntimeException()
    }

    override fun deleteMessage(dto: RemoteMessageDTO) {
        throw buildRuntimeException()
    }

    override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
        throw buildRuntimeException()
    }

    override fun getFile(dto: RemoteFileDTO): RemoteFileDTO {
        throw buildRuntimeException()
    }

    override fun deleteFile(dto: RemoteFileDTO) {
        throw buildRuntimeException()
    }
}