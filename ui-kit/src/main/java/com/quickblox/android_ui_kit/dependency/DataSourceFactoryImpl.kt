/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import android.content.Context
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceImpl
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl

@ExcludeFromCoverage
class DataSourceFactoryImpl(private val context: Context) : DataSourceFactory {
    override fun createRemote(): RemoteDataSource {
        return RemoteDataSourceImpl()
    }

    override fun createLocal(): LocalDataSource {
        return LocalDataSourceImpl()
    }

    override fun createLocalFile(): LocalFileDataSource {
        return LocalFileDataSourceImpl(context)
    }
}