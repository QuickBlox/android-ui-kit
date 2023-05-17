/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogsDTO
import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    @Throws(LocalDataSourceException::class)
    fun saveDialog(dto: LocalDialogDTO)

    @Throws(LocalDataSourceException::class)
    fun deleteDialog(dto: LocalDialogDTO)

    @Throws(LocalDataSourceException::class)
    fun getDialog(dto: LocalDialogDTO): LocalDialogDTO

    @Throws(LocalDataSourceException::class)
    fun getDialogs(): LocalDialogsDTO

    @Throws(LocalDataSourceException::class)
    fun getDialogsByName(name: String): LocalDialogsDTO

    fun getAllDialogs(): LocalDialogsDTO

    @Throws(LocalDataSourceException::class)
    suspend fun updateDialog(dto: LocalDialogDTO)

    @Throws(LocalDataSourceException::class)
    fun clearAll()

    @Throws(LocalDataSourceException::class)
    fun subscribeLocalSyncing(): Flow<Boolean>

    fun subscribeLocalUpdateDialogs(): Flow<LocalDialogDTO?>

    fun subscribeLocalSaveDialogs(): Flow<LocalDialogDTO?>

    @Throws(LocalDataSourceException::class)
    suspend fun setLocalSynced(synced: Boolean)

    @Throws(LocalDataSourceException::class)
    fun clearAllDialogsInLocal()
}