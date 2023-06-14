/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.dialogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.GROUP
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.PRIVATE
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.DialogsEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogsByNameUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogsUseCase
import com.quickblox.android_ui_kit.domain.usecases.LeaveDialogUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class DialogsViewModel : BaseViewModel() {
    private val TAG: String = DialogsViewModel::class.java.simpleName

    enum class DialogChangeType {
        ADDED, UPDATED, UPDATED_RANGE, DELETED
    }

    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()

    private var getDialogsJob: Job? = null

    private val _updatedDialogs = MutableLiveData<Pair<DialogChangeType, Int>>()
    val updatedDialogs: LiveData<Pair<DialogChangeType, Int>>
        get() = _updatedDialogs

    private val _syncing = MutableLiveData<Boolean>()
    val syncing: LiveData<Boolean>
        get() = _syncing

    val dialogs = arrayListOf<DialogEntity>()

    private var getDialogsUseCase = GetDialogsUseCase()

    init {
        subscribeConnection()
        subscribeToDialogsEvents()
    }

    fun createPrivateDialogEntity(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogType(PRIVATE)
        return entity
    }

    fun createGroupDialogEntity(): DialogEntity {
        val entity = DialogEntityImpl()
        entity.setDialogType(GROUP)
        return entity
    }

    fun getDialogs() {
        if (isNotConnected() || getDialogsJob?.isActive == true) {
            _updatedDialogs.value = Pair(DialogChangeType.UPDATED_RANGE, dialogs.lastIndex)
            return
        }

        dialogs.clear()
        _syncing.value = true
        getDialogsJob = viewModelScope.launch {
            getDialogsUseCase.execute().onCompletion {
                _syncing.value = false
            }.collect { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { dialog ->
                        if (isNotExistDialog(dialog, dialogs)) {
                            dialogs.add(dialog)
                            _updatedDialogs.value = Pair(DialogChangeType.ADDED, dialogs.lastIndex)
                        } else {
                            val index = dialogs.indexOf(dialog)
                            dialogs[index] = dialog
                            _updatedDialogs.value = Pair(DialogChangeType.UPDATED, index)
                        }
                    }
                }

                if (result.isFailure) {
                    val exception = result.exceptionOrNull()

                    exception?.let {
                        exception as DomainException
                        showError(exception.message)
                    }
                }
            }
        }
    }

    private fun isNotConnected(): Boolean {
        var isNotConnected = true
        viewModelScope.launch {
            isNotConnected = !connectionRepository.subscribe().first()
        }
        return isNotConnected
    }

    fun searchByName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val foundDialogs = GetDialogsByNameUseCase(name).execute()

                getDialogsJob?.cancel()

                foundDialogs?.let {
                    dialogs.clear()

                    for (dialog in it) {
                        dialogs.add(dialog)
                        _updatedDialogs.postValue(Pair(DialogChangeType.ADDED, dialogs.lastIndex))
                    }
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    fun leaveDialog(dialogEntity: DialogEntity) {
        viewModelScope.launch {
            runCatching {
                LeaveDialogUseCase(dialogEntity).execute()
                if (isNotExistDialog(dialogEntity, dialogs)) {
                    return@launch
                }

                val index = dialogs.indexOf(dialogEntity)
                dialogs.remove(dialogEntity)
                _updatedDialogs.postValue(Pair(DialogChangeType.DELETED, index))
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    private fun subscribeToDialogsEvents() {
        viewModelScope.launch {
            DialogsEventUseCase().execute().collect { dialogEntity ->
                if (isNotExistDialog(dialogEntity, dialogs)) {
                    return@collect
                }

                val index = dialogs.indexOf(dialogEntity)
                dialogEntity?.let { dialog ->
                    dialogs.removeAt(index)
                    dialogs.add(0, dialog)

                    _updatedDialogs.value = Pair(DialogChangeType.UPDATED, index)
                }
            }
        }
    }

    private fun isNotExistDialog(dialog: DialogEntity?, dialogs: ArrayList<DialogEntity>): Boolean {
        val index = dialogs.indexOf(dialog)
        return index == -1
    }

    override fun onConnected() {
        getDialogs()
    }

    override fun onDisconnected() {
        viewModelScope.launch {
            getDialogsUseCase.release()
            getDialogsJob?.cancel()
        }
    }
}