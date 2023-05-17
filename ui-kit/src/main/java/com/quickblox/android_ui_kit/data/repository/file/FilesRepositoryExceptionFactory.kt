/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.file

import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException

interface FilesRepositoryExceptionFactory {
    fun makeBy(code: LocalFileDataSourceException.Codes, description: String): FilesRepositoryException
    fun makeBy(code: RemoteDataSourceException.Codes, description: String): FilesRepositoryException
    fun makeNotFound(description: String): FilesRepositoryException
    fun makeUnexpected(description: String): FilesRepositoryException
    fun makeAlreadyExist(description: String): FilesRepositoryException
    fun makeUnauthorised(description: String): FilesRepositoryException
    fun makeIncorrectData(description: String): FilesRepositoryException
    fun makeRestrictedAccess(description: String): FilesRepositoryException
    fun makeConnectionFailed(description: String): FilesRepositoryException
    fun makeWriteAndRead(description: String): FilesRepositoryException
}
