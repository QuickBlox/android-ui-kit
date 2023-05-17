/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.data.source.local

import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException

interface LocalFileDataSource {
    @Throws(LocalFileDataSourceException::class)
    fun getFile(dto: LocalFileDTO): LocalFileDTO

    @Throws(LocalFileDataSourceException::class)
    fun getFileByUri(dto: LocalFileDTO): LocalFileDTO

    @Throws(LocalFileDataSourceException::class)
    fun createFile(dto: LocalFileDTO)

    @Throws(LocalFileDataSourceException::class)
    fun createFile(extension: String): LocalFileDTO

    @Throws(LocalFileDataSourceException::class)
    fun deleteFile(dto: LocalFileDTO)

    @Throws(LocalFileDataSourceException::class)
    fun clearAll()
}