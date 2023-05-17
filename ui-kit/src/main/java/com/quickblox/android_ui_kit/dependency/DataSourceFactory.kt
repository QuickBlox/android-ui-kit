/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource

interface DataSourceFactory {
    fun createRemote(): RemoteDataSource
    fun createLocal(): LocalDataSource
    fun createLocalFile(): LocalFileDataSource
}