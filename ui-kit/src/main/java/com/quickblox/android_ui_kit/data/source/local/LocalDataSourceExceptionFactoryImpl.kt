/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException.Codes.*

class LocalDataSourceExceptionFactoryImpl : LocalDataSourceExceptionFactory {
    override fun makeUnexpected(description: String): LocalDataSourceException {
        return LocalDataSourceException(UNEXPECTED, description)
    }

    override fun makeNotFound(description: String): LocalDataSourceException {
        return LocalDataSourceException(NOT_FOUND_ITEM, description)
    }

    override fun makeAlreadyExist(description: String): LocalDataSourceException {
        return LocalDataSourceException(ALREADY_EXIST, description)
    }
}