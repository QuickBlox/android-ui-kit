/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

open class ConnectionRepositorySpy(existConnection: Boolean = false) : ConnectionRepository {
    private val flow: MutableStateFlow<Boolean> = MutableStateFlow(existConnection)

    suspend fun setConnection(exist: Boolean) {
        flow.emit(exist)
    }

    override suspend fun connect() {
        flow.emit(true)
    }

    override suspend fun disconnect() {
        flow.emit(false)
    }

    override fun subscribe(): Flow<Boolean> {
        return flow
    }
}