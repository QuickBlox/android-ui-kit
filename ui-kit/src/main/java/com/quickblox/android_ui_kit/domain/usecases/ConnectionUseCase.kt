/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import android.util.Log
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ConnectionUseCase : BaseUseCase<Unit>() {
    private val TAG = ConnectionUseCase::javaClass.name
    private var connectionRepository = QuickBloxUiKit.getDependency().getConnectionRepository()

    private var scope = CoroutineScope(Dispatchers.IO)

    override suspend fun execute() {
        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        scope.launch {
            connect()
        }
    }

    private suspend fun connect() {
        try {
            connectionRepository.connect()
        } catch (exception: ConnectionRepositoryException) {
            val defaultMessage = ConnectionRepositoryException.Codes.UNEXPECTED.toString()
            Log.d(TAG, exception.message ?: defaultMessage)
        }
    }

    private suspend fun disconnect() {
        try {
            connectionRepository.disconnect()
        } catch (exception: ConnectionRepositoryException) {
            val defaultMessage = ConnectionRepositoryException.Codes.UNEXPECTED.toString()
            Log.d(TAG, exception.message ?: defaultMessage)
        }
    }

    override suspend fun release() {
        disconnect()
        scope.cancel()
    }
}