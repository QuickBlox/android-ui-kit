/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.user

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.UsersRepositoryException

interface UsersRepositoryExceptionFactory {
    fun makeBy(code: LocalDataSourceException.Codes, description: String): UsersRepositoryException
    fun makeBy(code: RemoteDataSourceException.Codes, description: String): UsersRepositoryException
    fun makeNotFound(description: String): UsersRepositoryException
    fun makeUnexpected(description: String): UsersRepositoryException
    fun makeAlreadyExist(description: String): UsersRepositoryException
    fun makeUnauthorised(description: String): UsersRepositoryException
    fun makeIncorrectData(description: String): UsersRepositoryException
    fun makeRestrictedAccess(description: String): UsersRepositoryException
    fun makeConnectionFailed(description: String): UsersRepositoryException
}
