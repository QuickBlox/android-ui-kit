/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.message

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException.Codes.*

class MessagesRepositoryExceptionFactoryImpl : MessagesRepositoryExceptionFactory {
    override fun makeBy(code: LocalDataSourceException.Codes, description: String): MessagesRepositoryException {
        return when (code) {
            LocalDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            LocalDataSourceException.Codes.ALREADY_EXIST -> makeAlreadyExist(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeBy(code: RemoteDataSourceException.Codes, description: String): MessagesRepositoryException {
        return when (code) {
            RemoteDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            RemoteDataSourceException.Codes.UNAUTHORISED -> makeUnauthorised(description)
            RemoteDataSourceException.Codes.INCORRECT_DATA -> makeIncorrectData(description)
            RemoteDataSourceException.Codes.RESTRICTED_ACCESS -> makeRestrictedAccess(description)
            RemoteDataSourceException.Codes.CONNECTION_FAILED -> makeConnectionFailed(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeNotFound(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(NOT_FOUND_ITEM, description)
    }

    override fun makeUnexpected(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(UNEXPECTED, description)
    }

    override fun makeAlreadyExist(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(ALREADY_EXIST, description)
    }

    override fun makeUnauthorised(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(UNAUTHORISED, description)
    }

    override fun makeIncorrectData(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(INCORRECT_DATA, description)
    }

    override fun makeRestrictedAccess(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(RESTRICTED_ACCESS, description)
    }

    override fun makeConnectionFailed(description: String): MessagesRepositoryException {
        return MessagesRepositoryException(CONNECTION_FAILED, description)
    }
}