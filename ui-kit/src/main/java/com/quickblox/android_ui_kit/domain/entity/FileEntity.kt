/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity

import android.net.Uri
import java.io.File

interface FileEntity {
    enum class FileTypes(val value: String) { IMAGE("image"), AUDIO("audio"), VIDEO("video"), FILE("application") }

    fun getFileType(): FileTypes?

    fun getUri(): Uri?
    fun setUri(uri: Uri?)

    fun getFile(): File?
    fun setFile(file: File?)

    fun setUrl(url: String?)
    fun getUrl(): String?

    fun getMimeType(): String?
    fun setMimeType(type: String?)

    fun getId(): Int?
    fun setId(id: Int?)
}