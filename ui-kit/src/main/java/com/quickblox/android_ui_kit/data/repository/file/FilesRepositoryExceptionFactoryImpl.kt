/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.file

import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException.Codes.*

class FilesRepositoryExceptionFactoryImpl : FilesRepositoryExceptionFactory {
    override fun makeBy(code: LocalFileDataSourceException.Codes, description: String): FilesRepositoryException {
        return when (code) {
            LocalFileDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            LocalFileDataSourceException.Codes.ALREADY_EXIST -> makeAlreadyExist(description)
            LocalFileDataSourceException.Codes.INCORRECT_DATA -> makeIncorrectData(description)
            LocalFileDataSourceException.Codes.WRITE_AND_READ -> makeWriteAndRead(description)
            LocalFileDataSourceException.Codes.RESTRICTED_ACCESS -> makeRestrictedAccess(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeBy(code: RemoteDataSourceException.Codes, description: String): FilesRepositoryException {
        return when (code) {
            RemoteDataSourceException.Codes.NOT_FOUND_ITEM -> makeNotFound(description)
            RemoteDataSourceException.Codes.UNAUTHORISED -> makeUnauthorised(description)
            RemoteDataSourceException.Codes.INCORRECT_DATA -> makeIncorrectData(description)
            RemoteDataSourceException.Codes.RESTRICTED_ACCESS -> makeRestrictedAccess(description)
            RemoteDataSourceException.Codes.CONNECTION_FAILED -> makeConnectionFailed(description)
            else -> makeUnexpected(description)
        }
    }

    override fun makeNotFound(description: String): FilesRepositoryException {
        return FilesRepositoryException(NOT_FOUND_ITEM, description)
    }

    override fun makeUnexpected(description: String): FilesRepositoryException {
        return FilesRepositoryException(UNEXPECTED, description)
    }

    override fun makeAlreadyExist(description: String): FilesRepositoryException {
        return FilesRepositoryException(ALREADY_EXIST, description)
    }

    override fun makeUnauthorised(description: String): FilesRepositoryException {
        return FilesRepositoryException(UNAUTHORISED, description)
    }

    override fun makeIncorrectData(description: String): FilesRepositoryException {
        return FilesRepositoryException(INCORRECT_DATA, description)
    }

    override fun makeRestrictedAccess(description: String): FilesRepositoryException {
        return FilesRepositoryException(RESTRICTED_ACCESS, description)
    }

    override fun makeConnectionFailed(description: String): FilesRepositoryException {
        return FilesRepositoryException(CONNECTION_FAILED, description)
    }

    override fun makeWriteAndRead(description: String): FilesRepositoryException {
        return FilesRepositoryException(WRITE_AND_READ, description)
    }
}