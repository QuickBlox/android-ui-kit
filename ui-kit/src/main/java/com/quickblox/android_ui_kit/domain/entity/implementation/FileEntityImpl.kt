/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity.implementation

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import java.io.File

class FileEntityImpl : FileEntity {
    private var uri: Uri? = null
    private var file: File? = null
    private var url: String? = null
    private var mimeType: String? = null
    private var id: Int? = null

    override fun getFileType(): FileEntity.FileTypes {
        return parseFileTypeFrom(mimeType)
    }

    private fun parseFileTypeFrom(contentType: String?): FileEntity.FileTypes {
        val splitType = contentType?.split("/")
        val sourceFileType = splitType?.get(0)

        when (sourceFileType?.lowercase()) {
            FileEntity.FileTypes.FILE.value -> {
                return FileEntity.FileTypes.FILE
            }
            FileEntity.FileTypes.AUDIO.value -> {
                return FileEntity.FileTypes.AUDIO
            }
            FileEntity.FileTypes.VIDEO.value -> {
                return FileEntity.FileTypes.VIDEO
            }
            FileEntity.FileTypes.IMAGE.value -> {
                return FileEntity.FileTypes.IMAGE
            }
            else -> {
                throw IllegalArgumentException("Error parse file type from $sourceFileType")
            }
        }
    }

    override fun getUri(): Uri? {
        return uri
    }

    override fun setUri(uri: Uri?) {
        this.uri = uri
    }

    override fun getFile(): File? {
        return file
    }

    override fun setFile(file: File?) {
        this.file = file
    }

    override fun setUrl(url: String?) {
        this.url = url
    }

    override fun getUrl(): String? {
        return url
    }

    override fun getMimeType(): String? {
        return mimeType
    }

    override fun setMimeType(type: String?) {
        this.mimeType = type
    }

    override fun getId(): Int? {
        return id
    }

    override fun setId(id: Int?) {
        this.id = id
    }
}