/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.stub.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.stub.BaseStub
import kotlinx.coroutines.flow.Flow

open class DialogsRepositoryStub : BaseStub(), DialogsRepository {
    override suspend fun saveDialogToLocal(entity: DialogEntity) {
        throw buildRuntimeException()
    }

    override fun createDialogInRemote(entity: DialogEntity): DialogEntity {
        throw buildRuntimeException()
    }

    override suspend fun updateDialogInLocal(entity: DialogEntity) {
        throw buildRuntimeException()
    }

    override fun updateDialogInRemote(entity: DialogEntity): DialogEntity {
        throw buildRuntimeException()
    }

    override fun getDialogFromLocal(dialogId: String): DialogEntity {
        throw buildRuntimeException()
    }

    override fun getDialogFromRemote(dialogId: String): DialogEntity {
        throw buildRuntimeException()
    }

    override fun getAllDialogsFromLocal(): Collection<DialogEntity> {
        throw buildRuntimeException()
    }

    override fun getAllDialogsFromRemote(): Flow<Result<DialogEntity>> {
        throw buildRuntimeException()
    }

    override fun getDialogsByName(name: String): Collection<DialogEntity> {
        throw buildRuntimeException()
    }

    override fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
        throw buildRuntimeException()
    }

    override fun removeUsersFromDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
        throw buildRuntimeException()
    }

    override suspend fun deleteDialogFromLocal(dialogId: String) {
        throw buildRuntimeException()
    }

    override fun subscribeLocalSaveDialogs(): Flow<DialogEntity?> {
        throw buildRuntimeException()
    }

    override fun leaveDialogFromRemote(entity: DialogEntity) {
        throw buildRuntimeException()
    }

    override fun clearAllDialogsInLocal() {
        throw buildRuntimeException()
    }

    override fun subscribeLocalSyncing(): Flow<Boolean> {
        throw buildRuntimeException()
    }

    override fun subscribeDialogEvents(): Flow<DialogEntity?> {
        throw buildRuntimeException()
    }

    override suspend fun setLocalSynced(synced: Boolean) {
        throw buildRuntimeException()
    }
}