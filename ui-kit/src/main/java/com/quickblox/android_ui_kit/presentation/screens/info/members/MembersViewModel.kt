/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.DialogEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetUsersFromDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.RemoveUsersFromDialogUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import com.quickblox.android_ui_kit.presentation.checkStringByRegex
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MembersViewModel : BaseViewModel() {
    private val userRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private var loadDialogJob: Job? = null

    private var dialogEntity: DialogEntity? = null
    private var dialogId: String = ""

    val loadedUsers = arrayListOf<UserEntity>()

    private val _loadedMembers = MutableLiveData<Unit>()
    val loadedMembers: LiveData<Unit>
        get() = _loadedMembers

    fun setDialogIdAndSubscribeToConnection(dialogId: String) {
        this.dialogId = dialogId
        subscribeConnection()
    }

    fun getLoggedUserId(): Int? {
        try {
            return userRepository.getLoggedUserId()
        } catch (exception: UsersRepositoryException) {
            showError(exception.message)
        }
        return null
    }

    fun getOwnerId(): Int? {
        return dialogEntity?.getOwnerId()
    }

    fun loadDialogAndMembers() {
        if (loadDialogJob?.isActive == true) {
            return
        }

        showLoading()
        loadDialogJob = viewModelScope.launch {
            runCatching {
                dialogEntity = GetDialogByIdUseCase(dialogId).execute()
                dialogEntity?.let {
                    val users = GetUsersFromDialogUseCase(it).execute()
                    loadedUsers.clear()

                    val regexUserName = QuickBloxUiKit.getRegexUserName()
                    if (regexUserName != null) {
                        checkUserNamesByRegex(users, regexUserName)
                    }

                    loadedUsers.addAll(users)
                    _loadedMembers.postValue(Unit)

                    subscribeToUpdateDialog(dialogId)
                    hideLoading()
                }
            }.onFailure { error ->
                hideLoading()
                showError(error.message)
            }
        }
    }

    private fun checkUserNamesByRegex(users: List<UserEntity>, regex: String) {
        for (user in users) {
            val isUserNameValid = user.getName()?.checkStringByRegex(regex)
            if (user.getName() == null || isUserNameValid == false) {
                user.setName("Unknown")
            }
        }
    }

    private fun subscribeToUpdateDialog(dialogId: String?) {
        if (dialogId.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            DialogEventUseCase(dialogId).execute().collect { entity ->
                dialogEntity = entity
                entity?.let {
                    val users = GetUsersFromDialogUseCase(it).execute()
                    loadedUsers.clear()
                    loadedUsers.addAll(users)
                    _loadedMembers.postValue(Unit)
                }
            }
        }
    }


    fun removeUserAndLoadMembersAndDialog(userId: Int) {
        showLoading()
        val userIds = listOf(userId)
        viewModelScope.launch {
            runCatching {
                dialogEntity?.let {
                    RemoveUsersFromDialogUseCase(it, userIds).execute()
                    hideLoading()
                    loadDialogAndMembers()
                }
            }.onFailure { error ->
                showError(error.message)
                hideLoading()
            }
        }
    }

    override fun onConnected() {
        loadDialogAndMembers()
    }
}