/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.message

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException

interface MessagesRepositoryExceptionFactory {
    fun makeBy(code: LocalDataSourceException.Codes, description: String): MessagesRepositoryException
    fun makeBy(code: RemoteDataSourceException.Codes, description: String): MessagesRepositoryException
    fun makeNotFound(description: String): MessagesRepositoryException
    fun makeUnexpected(description: String): MessagesRepositoryException
    fun makeAlreadyExist(description: String): MessagesRepositoryException
    fun makeUnauthorised(description: String): MessagesRepositoryException
    fun makeIncorrectData(description: String): MessagesRepositoryException
    fun makeRestrictedAccess(description: String): MessagesRepositoryException
    fun makeConnectionFailed(description: String): MessagesRepositoryException
}
