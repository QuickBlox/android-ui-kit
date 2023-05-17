/*
 * Created by Injoit on 27.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote

import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException

interface RemoteDataSourceExceptionFactory {
    fun makeBy(httpStatusCode: Int, description: String): RemoteDataSourceException
    fun makeUnexpected(description: String): RemoteDataSourceException
    fun makeNotFound(description: String): RemoteDataSourceException
    fun makeUnauthorised(description: String): RemoteDataSourceException
    fun makeIncorrectData(description: String): RemoteDataSourceException
    fun makeRestrictedAccess(description: String): RemoteDataSourceException
    fun makeConnectionFailed(description: String): RemoteDataSourceException
    fun makeAlreadyLogged(description: String): RemoteDataSourceException
}