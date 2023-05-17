/*
 * Created by Injoit on 27.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote

import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException.Codes.*

private const val badRequest = 400
private const val unauthorized = 401
private const val forbidden = 403
private const val notFound = 404
private const val unprocessableEntity = 422
private const val tooManyRequests = 429
private const val internalServerError = 500
private const val serviceUnavailable = 503

class RemoteDataSourceExceptionFactoryImpl : RemoteDataSourceExceptionFactory {
    override fun makeBy(httpStatusCode: Int, description: String): RemoteDataSourceException {
        return when (httpStatusCode) {
            badRequest -> makeUnexpected(description)
            unauthorized -> makeUnauthorised(description)
            forbidden -> makeRestrictedAccess(description)
            notFound -> makeNotFound(description)
            unprocessableEntity -> makeIncorrectData(description)
            tooManyRequests -> makeRestrictedAccess(description)
            internalServerError -> makeConnectionFailed(description)
            serviceUnavailable -> makeConnectionFailed(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeUnexpected(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(UNEXPECTED, description)
    }

    override fun makeNotFound(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(NOT_FOUND_ITEM, description)
    }

    override fun makeUnauthorised(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(UNAUTHORISED, description)
    }

    override fun makeIncorrectData(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(INCORRECT_DATA, description)
    }

    override fun makeRestrictedAccess(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(RESTRICTED_ACCESS, description)
    }

    override fun makeConnectionFailed(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(CONNECTION_FAILED, description)
    }

    override fun makeAlreadyLogged(description: String): RemoteDataSourceException {
        return RemoteDataSourceException(ALREADY_LOGGED, description)
    }
}