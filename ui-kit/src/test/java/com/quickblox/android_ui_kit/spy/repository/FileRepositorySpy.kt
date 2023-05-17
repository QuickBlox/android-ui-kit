/*
 * Created by Injoit on 7.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import com.quickblox.android_ui_kit.stub.repository.FileRepositoryStub
import org.mockito.Mockito.mock
import java.io.File

open class FileRepositorySpy : FileRepositoryStub() {
    override fun saveFileToLocal(entity: FileEntity) {}

    override fun saveFileToRemote(entity: FileEntity): FileEntity {
        val fileEntity: FileEntity = FileEntityImpl()
        fileEntity.setFile(File("test_path"))

        val uri: Uri = mock(Uri::class.java)
        fileEntity.setUri(uri)

        fileEntity.setUrl("https://test.com")
        fileEntity.setMimeType("audio/mpeg")

        return fileEntity
    }

    override fun getFileFromLocal(id: String): FileEntity {
        return FileEntityImpl()
    }

    override fun getFileFromRemote(id: String): FileEntity {
        return FileEntityImpl()
    }

    override fun deleteFileFromLocal(id: String) {
    }

    override fun deleteFileFromRemote(id: String) {
    }

    override fun createLocalFile(extension: String): FileEntity {
        val fileEntity: FileEntity = FileEntityImpl()
        fileEntity.setFile(File("test_path.$extension"))

        val uri: Uri = mock(Uri::class.java)
        fileEntity.setUri(uri)

        return fileEntity
    }

    override fun getFileFromLocalByUri(uri: Uri): FileEntity {
        val fileEntity: FileEntity = FileEntityImpl()
        fileEntity.setFile(File("test_path"))
        fileEntity.setUri(uri)

        return fileEntity
    }
}