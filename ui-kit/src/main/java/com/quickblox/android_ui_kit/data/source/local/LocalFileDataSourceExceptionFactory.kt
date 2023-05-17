/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException

interface LocalFileDataSourceExceptionFactory {
    fun makeUnexpected(description: String): LocalFileDataSourceException
    fun makeNotFound(description: String): LocalFileDataSourceException
    fun makeAlreadyExist(description: String): LocalFileDataSourceException
    fun makeIncorrectData(description: String): LocalFileDataSourceException
    fun makeWriteAndRead(description: String): LocalFileDataSourceException
    fun makeRestrictedAccess(description: String): LocalFileDataSourceException
}