/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.connection

import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException
import com.quickblox.android_ui_kit.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow

class ConnectionRepositoryImpl(private val remoteDataSource: RemoteDataSource) : ConnectionRepository {
    private val exceptionFactory: ConnectionRepositoryExceptionFactory = ConnectionRepositoryExceptionFactoryImpl()

    override suspend fun connect() {
        try {
            remoteDataSource.connect()
        } catch (exception: RemoteDataSourceException) {
            val defaultMessage = ConnectionRepositoryException.Codes.UNEXPECTED.toString()
            throw exceptionFactory.makeUnexpected(exception.message ?: defaultMessage)
        }
    }

    override suspend fun disconnect() {
        try {
            remoteDataSource.disconnect()
        } catch (exception: RemoteDataSourceException) {
            val defaultMessage = ConnectionRepositoryException.Codes.UNEXPECTED.toString()
            throw exceptionFactory.makeUnexpected(exception.message ?: defaultMessage)
        }
    }

    override fun subscribe(): Flow<Boolean> {
        return remoteDataSource.subscribeConnection()
    }
}