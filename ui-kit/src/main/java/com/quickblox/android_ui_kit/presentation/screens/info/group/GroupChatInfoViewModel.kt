/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.group

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.*
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GroupChatInfoViewModel : BaseViewModel() {
    private var loadedDialogEntity: DialogEntity? = null
    private var updatingDialogEntity: DialogEntity = DialogEntityImpl()
    private var loadDialogJob: Job? = null

    private var dialogAvatarUri: Uri? = null
    private var dialogId: String? = null

    private val _loadedAndUpdatedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedAndUpdatedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedAndUpdatedDialogEntity

    private val _leaveDialog = MutableLiveData<Unit>()
    val leaveDialog: LiveData<Unit>
        get() = _leaveDialog

    init {
        subscribeConnection()
    }

    fun setDialogId(dialogId: String) {
        this.dialogId = dialogId
        updatingDialogEntity.setDialogId(dialogId)
    }

    fun setDialogName(name: String) {
        updatingDialogEntity.setName(name)
    }

    fun updateDialog() {
        showLoading()
        viewModelScope.launch {
            runCatching {
                loadedDialogEntity = UpdateDialogUseCase(updatingDialogEntity).execute()
                _loadedAndUpdatedDialogEntity.postValue(loadedDialogEntity)

                setRequiredFieldsToUpdatingDialog()

                hideLoading()
            }.onFailure { exception ->
                showError(exception.message)
                hideLoading()
            }
        }
    }

    fun loadDialogEntity() {
        if (loadDialogJob?.isActive == true || dialogId.isNullOrEmpty()) {
            return
        }

        showLoading()
        loadDialogJob = viewModelScope.launch {
            runCatching {
                loadedDialogEntity = GetDialogByIdUseCase(dialogId!!).execute()
                _loadedAndUpdatedDialogEntity.postValue(loadedDialogEntity)
                setRequiredFieldsToUpdatingDialog()

                subscribeToUpdateDialog(dialogId)

                hideLoading()
            }.onFailure { error ->
                showError(error.message)
                hideLoading()
            }
        }
    }

    private fun setRequiredFieldsToUpdatingDialog() {
        updatingDialogEntity.setOwnerId(loadedDialogEntity?.getOwnerId())
        updatingDialogEntity.setDialogType(loadedDialogEntity?.getType())
        updatingDialogEntity.setName(loadedDialogEntity?.getName())
    }

    fun getDialogAvatarUri(): Uri? {
        return dialogAvatarUri
    }

    suspend fun createFileAndGetUri(): Uri? {
        try {
            val fileEntity = CreateLocalFileUseCase("jpg").execute()
            dialogAvatarUri = fileEntity?.getUri()
            return dialogAvatarUri
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun getFileBy(uri: Uri): FileEntity? {
        try {
            return GetLocalFileByUriUseCase(uri).execute()
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun uploadFileAndUpdateDialog(fileEntity: FileEntity?) {
        if (fileEntity == null) {
            showError("The file doesn't exist")
            return
        }

        showLoading()
        try {
            val remoteEntity = UploadFileUseCase(fileEntity).execute()
            updatingDialogEntity.setPhoto(remoteEntity?.getUrl())

            updateDialog()
        } catch (exception: DomainException) {
            hideLoading()
            showError(exception.message)
        }
    }

    private fun subscribeToUpdateDialog(dialogId: String?) {
        if (dialogId.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            DialogEventUseCase(dialogId).execute().collect { dialogEntity ->
                loadedDialogEntity = dialogEntity
                _loadedAndUpdatedDialogEntity.postValue(loadedDialogEntity)
                setRequiredFieldsToUpdatingDialog()
            }
        }
    }

    fun leaveDialog() {
        viewModelScope.launch {
            runCatching {
                loadedDialogEntity?.let {
                    LeaveDialogUseCase(it).execute()
                }
                _leaveDialog.value = Unit
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    override fun onConnected() {
        loadDialogEntity()
    }

    fun removeAvatar() {
        updatingDialogEntity.setPhoto("null")
        updateDialog()
    }
}