/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.create.users

import androidx.collection.arraySetOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.PRIVATE
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.usecases.CreateGroupDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreatePrivateDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadUsersByNameUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadUsersUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class UsersViewModel : BaseViewModel() {
    private val _addedUser = MutableLiveData<Unit>()
    val addedUser: LiveData<Unit>
        get() = _addedUser

    private var pagination: PaginationEntity = PaginationEntityImpl().apply {
        setPerPage(100)
    }

    private val _createdDialog = MutableLiveData<DialogEntity?>()
    val createdDialog: LiveData<DialogEntity?>
        get() = _createdDialog

    var isSearchingEvent = false
    private var searchingName: String? = null

    private var dialogEntity: DialogEntity? = null

    val loadedUsers = arrayListOf<UserEntity>()
    val selectedUsers = arraySetOf<UserEntity>()

    fun isPrivateDialog(): Boolean {
        return dialogEntity?.getType() == PRIVATE
    }

    fun setDialogEntity(dialogEntity: DialogEntity?) {
        this.dialogEntity = dialogEntity
    }

    fun loadAllUsers() {
        showLoading()
        searchingName = null
        viewModelScope.launch {
            LoadUsersUseCase(pagination).execute().catch {
                showError(it.message)
            }.onCompletion {
                hideLoading()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                    pagination = result.getOrThrow().second
                    _addedUser.value = Unit
                }
            }
        }
    }

    private fun loadUsersByName(name: String) {
        showLoading()
        searchingName = name
        viewModelScope.launch {
            LoadUsersByNameUseCase(pagination, name).execute().catch {
                showError(it.message)
            }.onCompletion {
                hideLoading()
            }.collect { result ->
                loadedUsers.add(result.getOrThrow().first)
                pagination = result.getOrThrow().second
                _addedUser.value = Unit
            }
        }
    }

    fun createPrivateDialog() {
        val participantId = getSelectedUserIdForPrivateDialog() ?: 0
        val customData = dialogEntity?.getCustomData()

        showLoading()
        viewModelScope.launch {
            runCatching {
                CreatePrivateDialogUseCase(participantId, customData).execute()
            }.onSuccess { result ->
                _createdDialog.value = result
                hideLoading()
            }.onFailure { error ->
                showError(error.message)
                hideLoading()
            }
        }
    }

    fun createGroupDialog() {
        val name = dialogEntity?.getName() ?: ""
        val participantIds = getSelectedUserIdsForGroupDialog()
        val avatarUrl = dialogEntity?.getPhoto()
        val customData = dialogEntity?.getCustomData()

        showLoading()
        viewModelScope.launch {
            runCatching {
                CreateGroupDialogUseCase(name, participantIds, avatarUrl, customData).execute()
            }.onSuccess { result ->
                _createdDialog.value = result
                hideLoading()
            }.onFailure { error ->
                showError(error.message)
                hideLoading()
            }
        }
    }

    fun loadAllCleanUsers() {
        isSearchingEvent = false
        resetPaginationAndCleanUsers()
        loadAllUsers()
    }

    private fun getSelectedUserIdsForGroupDialog(): List<Int> {
        val userIds = arrayListOf<Int>()
        selectedUsers.forEach { userEntity ->
            userEntity.getUserId()?.let {
                userIds.add(it)
            }
        }
        return userIds
    }

    private fun getSelectedUserIdForPrivateDialog(): Int? {
        var selectedId: Int? = null
        if (selectedUsers.isNotEmpty()) {
            selectedId = selectedUsers.first().getUserId()
        }
        return selectedId
    }

    fun cleanAndLoadUsersBy(name: String) {
        isSearchingEvent = true
        resetPaginationAndCleanUsers()
        loadUsersByName(name)
    }

    private fun resetPaginationAndCleanUsers() {
        loadedUsers.clear()
        pagination.setCurrentPage(1)
    }

    fun loadUsers() {
        val isNotExistNextPage = !pagination.nextPage()
        if (isNotExistNextPage) {
            return
        }

        if (isSearchingEvent) {
            loadUsersByName(searchingName.toString())
        } else {
            loadAllUsers()
        }
    }

    fun getDialogType(): DialogEntity.Types? {
        return dialogEntity?.getType()
    }
}