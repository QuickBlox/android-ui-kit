/*
 * Created by Injoit on 3.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import android.util.Log
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onCompletion

class SyncDialogsUseCase : BaseUseCase<Unit>() {
    private val TAG = SyncDialogsUseCase::class.java.simpleName

    private val dialogRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()

    private var scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    override suspend fun execute() {
        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        scope.launch {
            dialogRepository.subscribeDialogEvents().collect { dialogEntity ->
                dialogEntity?.let {
                    try {
                        dialogRepository.updateDialogInLocal(dialogEntity)
                    } catch (exception: DialogsRepositoryException) {
                        Log.d(TAG, exception.message ?: "unexpected exception")
                    }
                }
            }
        }

        scope.launch {
            connectionRepository.subscribe().collect { connectionExist ->
                syncJob?.cancel()
                setSyncInProgressAndClearLocal()

                if (connectionExist) {
                    syncJob = syncDialogs()
                }
            }
        }
    }

    private suspend fun setSyncInProgressAndClearLocal() {
        dialogRepository.setLocalSynced(false)
        dialogRepository.clearAllDialogsInLocal()
    }

    private suspend fun syncDialogs(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            dialogRepository.getAllDialogsFromRemote().onCompletion {
                if (coroutineContext.isActive) {
                    dialogRepository.setLocalSynced(true)
                }
            }.collect { result ->
                try {
                    val entity = result.getOrThrow()
                    dialogRepository.saveDialogToLocal(entity)
                } catch (repositoryException: DialogsRepositoryException) {
                    // TODO: need to add logic for reload error dialogs
                    repositoryException.toString()
                }
            }
        }
    }

    @ExcludeFromCoverage
    override suspend fun release() {
        setSyncInProgressAndClearLocal()
        scope.cancel()
    }
}