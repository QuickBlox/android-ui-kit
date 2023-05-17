/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.dialog

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException.Codes.*

class DialogsRepositoryExceptionFactoryImpl : DialogsRepositoryExceptionFactory {
    override fun makeBy(code: LocalDataSourceException.Codes, description: String): DialogsRepositoryException {
        return when (code) {
            LocalDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            LocalDataSourceException.Codes.ALREADY_EXIST -> makeAlreadyExist(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeBy(code: RemoteDataSourceException.Codes, description: String): DialogsRepositoryException {
        return when (code) {
            RemoteDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            RemoteDataSourceException.Codes.UNAUTHORISED -> makeUnauthorised(description)
            RemoteDataSourceException.Codes.INCORRECT_DATA -> makeIncorrectData(description)
            RemoteDataSourceException.Codes.RESTRICTED_ACCESS -> makeRestrictedAccess(description)
            RemoteDataSourceException.Codes.CONNECTION_FAILED -> makeConnectionFailed(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeNotFound(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(NOT_FOUND_ITEM, description)
    }

    override fun makeUnexpected(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(UNEXPECTED, description)
    }

    override fun makeAlreadyExist(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(ALREADY_EXIST, description)
    }

    override fun makeUnauthorised(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(UNAUTHORISED, description)
    }

    override fun makeIncorrectData(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(INCORRECT_DATA, description)
    }

    override fun makeRestrictedAccess(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(RESTRICTED_ACCESS, description)
    }

    override fun makeConnectionFailed(description: String): DialogsRepositoryException {
        return DialogsRepositoryException(CONNECTION_FAILED, description)
    }
}