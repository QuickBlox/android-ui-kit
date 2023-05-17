/*
 * Created by Injoit on 30.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import android.net.Uri
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import com.quickblox.android_ui_kit.domain.usecases.base.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetLocalFileByUriUseCase(private val uri: Uri) : BaseUseCase<FileEntity?>() {
    private val filesRepository = QuickBloxUiKit.getDependency().getFilesRepository()

    override suspend fun execute(): FileEntity? {
        var loadedFIle: FileEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                loadedFIle = filesRepository.getFileFromLocalByUri(uri)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
        return loadedFIle
    }
}