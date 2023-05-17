/*
 * Created by Injoit on 3.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GetDialogsUseCase : FlowUseCase<Result<DialogEntity>>() {
    private val dialogRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()

    private var localUpdateDialogScope = CoroutineScope(Dispatchers.IO)
    private var connectionScope = CoroutineScope(Dispatchers.IO)

    override suspend fun execute(): Flow<Result<DialogEntity>> {
        if (isScopeNotActive(localUpdateDialogScope)) {
            localUpdateDialogScope = CoroutineScope(Dispatchers.IO)
        }

        if (isScopeNotActive(connectionScope)) {
            connectionScope = CoroutineScope(Dispatchers.IO)
        }

        return channelFlow {
            connectionScope.launch {
                connectionRepository.subscribe().collect { connected ->
                    val notConnected = !connected
                    val isNotClosedFlow = !isClosedForSend
                    if (notConnected && isNotClosedFlow) {
                        send(Result.failure(DomainException("Connection is not available")))
                    }
                }
            }

            getAllDialogsFromCache().collect { dialogEntity ->
                send(Result.success(dialogEntity))
            }

            val notSynced = !dialogRepository.subscribeLocalSyncing().first()
            if (notSynced) {
                subscribeLocalSyncing().collect { dialogEntity ->
                    dialogEntity?.let {
                        send(Result.success(dialogEntity))
                    }
                }
            }
        }.buffer(1)
    }

    private suspend fun subscribeLocalSyncing(): Flow<DialogEntity?> {
        return channelFlow {
            dialogRepository.subscribeLocalSyncing().collect { synced ->
                if (synced) {
                    localUpdateDialogScope.cancel()

                    getAllDialogsFromCache().onCompletion {
                        currentCoroutineContext().cancel()
                    }.collect {
                        send(it)
                    }
                } else {
                    if (isScopeNotActive(localUpdateDialogScope)) {
                        localUpdateDialogScope = CoroutineScope(Dispatchers.IO)
                    }
                    localUpdateDialogScope.launch {
                        dialogRepository.subscribeLocalSaveDialogs().collect {
                            send(it)
                        }
                    }
                }
            }
        }.buffer(1)
    }

    private suspend fun getAllDialogsFromCache(): Flow<DialogEntity> {
        return channelFlow {
            dialogRepository.getAllDialogsFromLocal().forEach { dialogEntity ->
                send(dialogEntity)
            }
        }
    }

    override suspend fun release() {
        localUpdateDialogScope.cancel()
        connectionScope.cancel()
    }
}