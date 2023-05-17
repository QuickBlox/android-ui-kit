/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException

interface LocalDataSourceExceptionFactory {
    fun makeUnexpected(description: String): LocalDataSourceException
    fun makeNotFound(description: String): LocalDataSourceException
    fun makeAlreadyExist(description: String): LocalDataSourceException
}