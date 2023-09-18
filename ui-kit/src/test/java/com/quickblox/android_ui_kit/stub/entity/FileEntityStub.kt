/*
 * Created by Injoit on 13.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.entity

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.stub.BaseStub
import java.io.File

open class FileEntityStub : BaseStub(), FileEntity {
    override fun getFileType(): FileEntity.FileTypes? {
        throw buildRuntimeException()
    }

    override fun getUri(): Uri? {
        throw buildRuntimeException()
    }

    override fun setUri(uri: Uri?) {
        throw buildRuntimeException()
    }

    override fun getFile(): File? {
        throw buildRuntimeException()
    }

    override fun setFile(file: File?) {
        throw buildRuntimeException()
    }

    override fun setUrl(url: String?) {
        throw buildRuntimeException()
    }

    override fun getUrl(): String? {
        throw buildRuntimeException()
    }

    override fun setUid(uid: String?) {
        throw buildRuntimeException()
    }

    override fun getUid(): String? {
        throw buildRuntimeException()    }

    override fun setMimeType(type: String?) {
        throw buildRuntimeException()
    }

    override fun getId(): Int? {
        throw buildRuntimeException()
    }

    override fun setId(id: Int?) {
        throw buildRuntimeException()
    }

    override fun getMimeType(): String? {
        throw buildRuntimeException()
    }
}