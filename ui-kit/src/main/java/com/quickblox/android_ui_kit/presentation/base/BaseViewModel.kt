/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.QuickBloxUiKit
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()
    private var connectionJob: Job? = null

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    protected fun subscribeConnection() {
        if (connectionJob?.isActive == true) {
            return
        }

        connectionJob = viewModelScope.launch {
            connectionRepository.subscribe().collect { connected ->
                if (connected) {
                    onConnected()
                } else {
                    onDisconnected()
                }
            }
        }
    }

    protected fun showError(message: String?) {
        message?.let {
            _errorMessage.postValue(it)
        }
    }

    protected fun showLoading() {
        _loading.postValue(true)
    }

    protected fun hideLoading() {
        _loading.postValue(false)
    }

    protected open fun onConnected() {}

    protected open fun onDisconnected() {}
}