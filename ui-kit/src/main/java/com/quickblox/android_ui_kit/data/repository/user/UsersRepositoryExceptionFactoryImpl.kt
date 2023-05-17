/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.user

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException.Codes.*

class UsersRepositoryExceptionFactoryImpl : UsersRepositoryExceptionFactory {
    override fun makeBy(code: LocalDataSourceException.Codes, description: String): UsersRepositoryException {
        return when (code) {
            LocalDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            LocalDataSourceException.Codes.ALREADY_EXIST -> makeAlreadyExist(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeBy(code: RemoteDataSourceException.Codes, description: String): UsersRepositoryException {
        return when (code) {
            RemoteDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            RemoteDataSourceException.Codes.UNAUTHORISED -> makeUnauthorised(description)
            RemoteDataSourceException.Codes.INCORRECT_DATA -> makeIncorrectData(description)
            RemoteDataSourceException.Codes.RESTRICTED_ACCESS -> makeRestrictedAccess(description)
            RemoteDataSourceException.Codes.CONNECTION_FAILED -> makeConnectionFailed(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeNotFound(description: String): UsersRepositoryException {
        return UsersRepositoryException(NOT_FOUND_ITEM, description)
    }

    override fun makeUnexpected(description: String): UsersRepositoryException {
        return UsersRepositoryException(UNEXPECTED, description)
    }

    override fun makeAlreadyExist(description: String): UsersRepositoryException {
        return UsersRepositoryException(ALREADY_EXIST, description)
    }

    override fun makeUnauthorised(description: String): UsersRepositoryException {
        return UsersRepositoryException(UNAUTHORISED, description)
    }

    override fun makeIncorrectData(description: String): UsersRepositoryException {
        return UsersRepositoryException(INCORRECT_DATA, description)
    }

    override fun makeRestrictedAccess(description: String): UsersRepositoryException {
        return UsersRepositoryException(RESTRICTED_ACCESS, description)
    }

    override fun makeConnectionFailed(description: String): UsersRepositoryException {
        return UsersRepositoryException(CONNECTION_FAILED, description)
    }
}