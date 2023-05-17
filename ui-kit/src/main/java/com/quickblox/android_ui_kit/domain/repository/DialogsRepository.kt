/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import kotlinx.coroutines.flow.Flow

interface DialogsRepository {
    @Throws(DialogsRepositoryException::class)
    suspend fun saveDialogToLocal(entity: DialogEntity)

    @Throws(DialogsRepositoryException::class)
    fun createDialogInRemote(entity: DialogEntity): DialogEntity

    @Throws(DialogsRepositoryException::class)
    suspend fun updateDialogInLocal(entity: DialogEntity)

    @Throws(DialogsRepositoryException::class)
    fun updateDialogInRemote(entity: DialogEntity): DialogEntity

    @Throws(DialogsRepositoryException::class)
    fun getDialogFromLocal(dialogId: String): DialogEntity

    @Throws(DialogsRepositoryException::class)
    fun getDialogFromRemote(dialogId: String): DialogEntity

    @Throws(DialogsRepositoryException::class)
    fun getAllDialogsFromLocal(): Collection<DialogEntity>

    @Throws(DialogsRepositoryException::class)
    fun getAllDialogsFromRemote(): Flow<Result<DialogEntity>>

    @Throws(DialogsRepositoryException::class)
    fun getDialogsByName(name: String): Collection<DialogEntity>

    @Throws(DialogsRepositoryException::class)
    fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity

    @Throws(DialogsRepositoryException::class)
    fun removeUsersFromDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity

    @Throws(DialogsRepositoryException::class)
    suspend fun deleteDialogFromLocal(dialogId: String)

    fun subscribeLocalSaveDialogs(): Flow<DialogEntity?>

    @Throws(DialogsRepositoryException::class)
    fun leaveDialogFromRemote(entity: DialogEntity)

    fun subscribeLocalSyncing(): Flow<Boolean>

    fun subscribeDialogEvents(): Flow<DialogEntity?>

    suspend fun setLocalSynced(synced: Boolean)

    fun clearAllDialogsInLocal()
}