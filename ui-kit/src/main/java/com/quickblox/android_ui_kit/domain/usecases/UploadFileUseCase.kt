/*
 * Created by Injoit on 30.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadFileUseCase(private val fileEntity: FileEntity) : BaseUseCase<FileEntity?>() {
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()

    override suspend fun execute(): FileEntity? {
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
}