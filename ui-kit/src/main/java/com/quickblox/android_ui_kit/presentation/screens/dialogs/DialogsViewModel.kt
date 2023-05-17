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
    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()

    private var getDialogsJob: Job? = null

    private val _updatedDialogs = MutableLiveData<Unit>()
    val updatedDialogs: LiveData<Unit>
        get() = _updatedDialogs

    private val _syncing = MutableLiveData<Boolean>()
    val syncing: LiveData<Boolean>
        get() = _syncing

    val dialogs = arrayListOf<DialogEntity>()

    private var subscribeDialogsEventUseCase = DialogsEventUseCase()
    private var getDialogsUseCase = GetDialogsUseCase()

    init {
        subscribeConnection()
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
            return
        }

        dialogs.clear()
        _syncing.value = true
        getDialogsJob = viewModelScope.launch {
            getDialogsUseCase.execute().onCompletion {
                subscribeToDialogsEvents()
                _syncing.value = false
            }.collect { result ->
                if (result.isSuccess) {
                    val dialog = result.getOrNull()
                    dialog?.let {
                        dialogs.remove(it)
                        dialogs.add(it)
                        _updatedDialogs.value = Unit
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

                foundDialogs?.let {
                    dialogs.clear()
                    dialogs.addAll(it)
                }
                _updatedDialogs.postValue(Unit)
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    fun leaveDialog(dialogEntity: DialogEntity) {
        viewModelScope.launch {
            runCatching {
                LeaveDialogUseCase(dialogEntity).execute()
                dialogs.remove(dialogEntity)
                _updatedDialogs.value = Unit
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    private fun subscribeToDialogsEvents() {
        viewModelScope.launch {
            subscribeDialogsEventUseCase.execute().collect { dialogEntity ->
                dialogs.remove(dialogEntity)
                dialogEntity?.let {
                    dialogs.add(0, it)
                }
                _updatedDialogs.value = Unit
            }
        }
    }

    override fun onConnected() {
        getDialogs()
    }

    override fun onDisconnected() {
        viewModelScope.launch {
            getDialogsUseCase.release()
            subscribeDialogsEventUseCase.release()
            getDialogsJob?.cancel()
        }
    }
}