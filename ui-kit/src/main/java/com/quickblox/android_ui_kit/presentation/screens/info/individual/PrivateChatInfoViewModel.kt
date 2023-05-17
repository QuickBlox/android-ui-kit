/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.individual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.LeaveDialogUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PrivateChatInfoViewModel : BaseViewModel() {
    private var dialogEntity: DialogEntity? = null
    private var dialogId: String? = null
    private var loadDialogJob: Job? = null

    private val _loadedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedDialogEntity

    private val _leaveDialog = MutableLiveData<Unit>()
    val leaveDialog: LiveData<Unit>
        get() = _leaveDialog

    init {
        subscribeConnection()
    }

    fun setDialogId(dialogId: String) {
        this.dialogId = dialogId
    }

    fun loadDialogEntity() {
        if (loadDialogJob?.isActive == true || dialogId.isNullOrEmpty()) {
            return
        }

        showLoading()
        loadDialogJob = viewModelScope.launch {
            runCatching {
                dialogEntity = GetDialogByIdUseCase(dialogId!!).execute()
                _loadedDialogEntity.postValue(dialogEntity)
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    fun leaveDialog() {
        viewModelScope.launch {
            runCatching {
                dialogEntity?.let {
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
}