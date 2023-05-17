/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.source

import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogsDTO
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.stub.BaseStub
import kotlinx.coroutines.flow.Flow

open class LocalDataSourceStub : BaseStub(), LocalDataSource {
    override fun saveDialog(dto: LocalDialogDTO) {
        throw buildRuntimeException()
    }

    override fun deleteDialog(dto: LocalDialogDTO) {
        throw buildRuntimeException()
    }

    override fun getDialog(dto: LocalDialogDTO): LocalDialogDTO {
        throw buildRuntimeException()
    }

    override fun getDialogs(): LocalDialogsDTO {
        throw buildRuntimeException()
    }

    override fun getDialogsByName(name: String): LocalDialogsDTO {
        throw buildRuntimeException()
    }

    override fun getAllDialogs(): LocalDialogsDTO {
        throw buildRuntimeException()
    }

    override suspend fun updateDialog(dto: LocalDialogDTO) {
        throw buildRuntimeException()
    }

    override fun clearAll() {
        throw buildRuntimeException()
    }

    override fun subscribeLocalSyncing(): Flow<Boolean> {
        throw buildRuntimeException()
    }

    override fun subscribeLocalUpdateDialogs(): Flow<LocalDialogDTO?> {
        throw buildRuntimeException()
    }

    override fun subscribeLocalSaveDialogs(): Flow<LocalDialogDTO?> {
        throw buildRuntimeException()
    }

    override suspend fun setLocalSynced(synced: Boolean) {
        throw buildRuntimeException()
    }

    override fun clearAllDialogsInLocal() {
        throw buildRuntimeException()
    }
}