/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.stub.repository

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.repository.FilesRepository

open class FileRepositoryStub : FilesRepository {
    override fun saveFileToLocal(entity: FileEntity) {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun saveFileToRemote(entity: FileEntity): FileEntity {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun getFileFromLocal(id: String): FileEntity {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun getFileFromRemote(id: String): FileEntity {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun deleteFileFromLocal(id: String) {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun deleteFileFromRemote(id: String) {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun createLocalFile(extension: String): FileEntity {
        throw RuntimeException("expected: override, actual: not override")
    }

    override fun getFileFromLocalByUri(uri: Uri): FileEntity {
        throw RuntimeException("expected: override, actual: not override")
    }
}