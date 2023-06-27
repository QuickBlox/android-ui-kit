/*
 * Created by Injoit on 30.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


private const val MAX_MEGABYTES_FILE_LENGTH = 10

class UploadFileUseCase(private val fileEntity: FileEntity) : BaseUseCase<FileEntity?>() {
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()

    override suspend fun execute(): FileEntity? {
        if (isNotCorrectFile(fileEntity.getFile())) {
            throw DomainException("The file is NULL or size in incorrect")
        }

        if (isNotCorrectSize(fileEntity.getFile()!!, MAX_MEGABYTES_FILE_LENGTH)) {
            throw DomainException("The file size more then max supported $MAX_MEGABYTES_FILE_LENGTH megabytes")
        }

        var uploadedFile: FileEntity? = null
        withContext(Dispatchers.IO) {
            runCatching {
                uploadedFile = filesRepository.saveFileToRemote(fileEntity)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
        return uploadedFile
    }

    @VisibleForTesting
    fun isNotCorrectFile(file: File?): Boolean {
        return file == null || file.length() <= 0
    }

    @VisibleForTesting
    fun isNotCorrectSize(file: File, maxMegaBytesFileLength: Int): Boolean {
        val bytesFileLength = file.length()
        val kiloBytesFileLength = bytesFileLength / 1024
        val megaBytesFileLength = kiloBytesFileLength / 1024

        return megaBytesFileLength > maxMegaBytesFileLength
    }
}