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

class CreateLocalFileUseCase(private val extension: String) : BaseUseCase<FileEntity?>() {
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()

    override suspend fun execute(): FileEntity? {
        var createdFile: FileEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                createdFile = filesRepository.createLocalFile(extension)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return createdFile
    }
}