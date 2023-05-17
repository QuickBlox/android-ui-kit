/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.dialog

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException

interface DialogsRepositoryExceptionFactory {
    fun makeBy(code: LocalDataSourceException.Codes, description: String): DialogsRepositoryException
    fun makeBy(code: RemoteDataSourceException.Codes, description: String): DialogsRepositoryException
    fun makeNotFound(description: String): DialogsRepositoryException
    fun makeUnexpected(description: String): DialogsRepositoryException
    fun makeAlreadyExist(description: String): DialogsRepositoryException
    fun makeUnauthorised(description: String): DialogsRepositoryException
    fun makeIncorrectData(description: String): DialogsRepositoryException
    fun makeRestrictedAccess(description: String): DialogsRepositoryException
    fun makeConnectionFailed(description: String): DialogsRepositoryException
}
