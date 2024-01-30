/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogsDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

@ExcludeFromCoverage
class LocalDataSourceImpl : LocalDataSource {
    private val exceptionFactory = LocalDataSourceExceptionFactoryImpl()

    private val dialogsSyncedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val dialogsUpdateFlow = MutableSharedFlow<LocalDialogDTO?>(0)
    private val dialogsSaveFlow = MutableSharedFlow<LocalDialogDTO?>(0)

    private val comparator = Comparator<LocalDialogDTO> { firstDTO, secondDTO ->
        val firstLastMessageDateSent: Long = firstDTO?.lastMessageDateSent ?: System.currentTimeMillis()
        val secondLastMessageDateSent: Long = secondDTO?.lastMessageDateSent ?: System.currentTimeMillis()

        secondLastMessageDateSent.compareTo(firstLastMessageDateSent)
    }

    private val dialogs = Collections.synchronizedSet(HashSet<LocalDialogDTO>())

    override fun saveDialog(dto: LocalDialogDTO) {
        val foundDto = dialogs.find {
            it.id == dto.id
        }

        val lastMessageDateSentOfFoundDto = foundDto?.lastMessageDateSent ?: 0
        val lastMessageDateSentOfDto = dto.lastMessageDateSent ?: 0

        val isDtoRelevantThanInCache = lastMessageDateSentOfDto > lastMessageDateSentOfFoundDto
        if (isDtoRelevantThanInCache) {
            dialogs.remove(foundDto)
        }

        val isNotAdded = !dialogs.add(dto)
        if (isNotAdded) {
            throw exceptionFactory.makeUnexpected("Dialog not saved")
        }
        notifySaveDialogFlow(dto)
    }

    private fun notifySaveDialogFlow(dto: LocalDialogDTO) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogsSaveFlow.emit(dto)
        }
    }

    override fun deleteDialog(dto: LocalDialogDTO) {
        val notExist = !dialogs.contains(dto)
        if (notExist) {
            throw exceptionFactory.makeNotFound("Dialog not found")
        }
        val isNotDeleted = !dialogs.remove(dto)
        if (isNotDeleted) {
            throw exceptionFactory.makeUnexpected("Dialog not deleted")
        }
    }

    override fun getDialog(dto: LocalDialogDTO): LocalDialogDTO {
        val notExist = !dialogs.contains(dto)
        if (notExist) {
            throw exceptionFactory.makeNotFound("Dialog not found")
        }
        return dialogs.first { localDialogDTO ->
            localDialogDTO.id == dto.id
        }
    }

    override fun getDialogs(): LocalDialogsDTO {
        val dialogsDTO = LocalDialogsDTO()

        val sortedList = dialogs.sortedWith(comparator)
        dialogsDTO.dialogs = sortedList.toList()

        return dialogsDTO
    }

    override fun getDialogsByName(name: String): LocalDialogsDTO {
        val dialogsDTO = LocalDialogsDTO()

        val foundDialogs = dialogs.filter { dialog -> dialog.name?.contains(name) == true }
        val sortedList = foundDialogs.sortedWith(comparator)
        dialogsDTO.dialogs = sortedList.toList()

        return dialogsDTO
    }

    override fun getAllDialogs(): LocalDialogsDTO {
        val dto = LocalDialogsDTO()
        dto.dialogs = dialogs.toList()

        return dto
    }

    override suspend fun updateDialog(dto: LocalDialogDTO) {
        replaceDialog(dto)

        dialogsUpdateFlow.emit(dto)
    }

    private fun replaceDialog(dto: LocalDialogDTO) {
        dialogs.remove(dto)
        dialogs.add(dto)
    }

    override fun clearAll() {
        dialogs.clear()
    }

    override fun subscribeLocalSyncing(): Flow<Boolean> {
        return dialogsSyncedFlow
    }

    override fun subscribeLocalUpdateDialogs(): Flow<LocalDialogDTO?> {
        return dialogsUpdateFlow
    }

    override fun subscribeLocalSaveDialogs(): Flow<LocalDialogDTO?> {
        return dialogsSaveFlow
    }

    override suspend fun setLocalSynced(synced: Boolean) {
        dialogsSyncedFlow.emit(synced)
    }

    override fun clearAllDialogsInLocal() {
        dialogs.clear()
    }
}