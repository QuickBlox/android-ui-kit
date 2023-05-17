/*
 * Created by Injoit on 19.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.info.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.usecases.AddUsersToDialogUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetAllUsersWithExcludeByIdsUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadUsersByNameWithExcludeByIdsUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class AddMembersViewModel : BaseViewModel() {
    private var dialogEntity: DialogEntity? = null
    private var dialogId: String? = null
    private var loadDialogJob: Job? = null
    private var searchingName: String? = null
    var isSearchingEvent = false
    val loadedUsers = arrayListOf<UserEntity>()

    private var pagination: PaginationEntity = PaginationEntityImpl().apply {
        setPerPage(100)
    }

    private val _updateList = MutableLiveData<Unit>()
    val updateList: LiveData<Unit>
        get() = _updateList

    private val _loadedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedDialogEntity

    private val _leaveDialog = MutableLiveData<Unit>()
    val leaveDialog: LiveData<Unit>
        get() = _leaveDialog

    fun setDialogId(dialogId: String) {
        this.dialogId = dialogId
        subscribeConnection()
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

    fun loadAllUsers() {
        val excludeIds = dialogEntity?.getParticipantIds()
        if (excludeIds == null) {
            return
        }

        showLoading()
        searchingName = null

        viewModelScope.launch {
            GetAllUsersWithExcludeByIdsUseCase(pagination, excludeIds).execute().catch {
                showError(it.message)
            }.onCompletion {
                hideLoading()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedUsers.add(result.getOrThrow().first)
                    pagination = result.getOrThrow().second
                    _updateList.value = Unit
                }
            }
        }
    }

    private fun loadUsersByName(name: String) {
        val excludeIds = dialogEntity?.getParticipantIds()
        if (excludeIds == null) {
            return
        }

        showLoading()
        searchingName = name
        viewModelScope.launch {
            LoadUsersByNameWithExcludeByIdsUseCase(pagination, name, excludeIds).execute().catch {
                showError(it.message)
            }.onCompletion {
                hideLoading()
            }.collect { result ->
                loadedUsers.add(result.getOrThrow().first)
                pagination = result.getOrThrow().second
                _updateList.value = Unit
            }
        }
    }

    fun loadAllCleanUsers() {
        isSearchingEvent = false
        resetPaginationAndCleanUsers()
        loadAllUsers()
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

    fun addUser(user: UserEntity) {
        showLoading()
        val userIds = arrayListOf<Int>()
        user.getUserId()?.let {
            userIds.add(it)
        }

        viewModelScope.launch {
            runCatching {
                dialogEntity?.let {
                    dialogEntity = AddUsersToDialogUseCase(it, userIds).execute()

                    loadedUsers.remove(user)
                    _updateList.postValue(Unit)
                    hideLoading()
                }
            }.onFailure { error ->
                showError(error.message)
                hideLoading()
            }
        }
    }

    override fun onConnected() {
        loadDialogEntity()
    }
}