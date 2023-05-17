/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    suspend fun connect()

    suspend fun disconnect()

    fun subscribe(): Flow<Boolean>
}