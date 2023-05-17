/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.stub.repository.DialogsRepositoryStub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

open class DialogsRepositorySpy(
    localDialogsCount: Int = 10, private val remoteDialogsCount: Int = 10, synced: Boolean = false
) : DialogsRepositoryStub() {
    private val localSyncedFlow: MutableStateFlow<Boolean> = MutableStateFlow(synced)
    private val updateDialogsFlow: MutableStateFlow<DialogEntity?> = MutableStateFlow(null)
    private val savedDialogs = mutableListOf<DialogEntity>()

    init {
        savedDialogs.addAll(buildStubXDialogs(localDialogsCount))
    }

    override suspend fun setLocalSynced(synced: Boolean) {
        localSyncedFlow.emit(synced)
    }

    override fun subscribeLocalSaveDialogs(): Flow<DialogEntity?> {
        return updateDialogsFlow
    }

    override fun subscribeLocalSyncing(): Flow<Boolean> {
        return localSyncedFlow
    }

    override fun getAllDialogsFromLocal(): Collection<DialogEntity> {
        return savedDialogs
    }

    override fun getDialogsByName(name: String): Collection<DialogEntity> {
        return savedDialogs
    }

    override fun getAllDialogsFromRemote(): Flow<Result<DialogEntity>> {
        return flow {
            buildStubXDialogs(remoteDialogsCount).forEach { dialogEntity ->
                emit(Result.success(dialogEntity))
            }
        }
    }

    override fun clearAllDialogsInLocal() {
        savedDialogs.clear()
    }

    private fun buildStubXDialogs(dialogsCount: Int): List<DialogEntity> {
        val dialogs = mutableListOf<DialogEntity>()
        for (index in 1..dialogsCount) {
            dialogs.add(DialogEntitySpy())
        }
        return dialogs
    }

    override suspend fun saveDialogToLocal(entity: DialogEntity) {
        savedDialogs.add(entity)
        updateDialogsFlow.emit(entity)
    }

    override fun subscribeDialogEvents(): Flow<DialogEntity?> {
        return flow { }
    }

    override suspend fun updateDialogInLocal(entity: DialogEntity) {

    }

    override fun getDialogFromLocal(dialogId: String): DialogEntity {
        return DialogEntitySpy()
    }
}