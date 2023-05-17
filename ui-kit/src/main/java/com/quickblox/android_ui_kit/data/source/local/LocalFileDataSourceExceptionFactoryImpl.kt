/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException

class LocalFileDataSourceExceptionFactoryImpl : LocalFileDataSourceExceptionFactory {
    override fun makeUnexpected(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.UNEXPECTED, description)
    }

    override fun makeNotFound(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.NOT_FOUND_ITEM, description)
    }

    override fun makeAlreadyExist(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.ALREADY_EXIST, description)
    }

    override fun makeIncorrectData(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.INCORRECT_DATA, description)
    }

    override fun makeWriteAndRead(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.WRITE_AND_READ, description)
    }

    override fun makeRestrictedAccess(description: String): LocalFileDataSourceException {
        return LocalFileDataSourceException(LocalFileDataSourceException.Codes.RESTRICTED_ACCESS, description)
    }
}