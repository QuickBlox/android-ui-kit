/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.repository

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException

interface FilesRepository {
    @Throws(FilesRepositoryException::class)
    fun saveFileToLocal(entity: FileEntity)

    @Throws(FilesRepositoryException::class)
    fun saveFileToRemote(entity: FileEntity): FileEntity

    @Throws(FilesRepositoryException::class)
    fun getFileFromLocal(id: String): FileEntity

    @Throws(FilesRepositoryException::class)
    fun getFileFromRemote(id: String): FileEntity

    @Throws(FilesRepositoryException::class)
    fun deleteFileFromLocal(id: String)

    @Throws(FilesRepositoryException::class)
    fun deleteFileFromRemote(id: String)

    @Throws(FilesRepositoryException::class)
    fun createLocalFile(extension: String): FileEntity

    @Throws(FilesRepositoryException::class)
    fun getFileFromLocalByUri(uri: Uri): FileEntity
}