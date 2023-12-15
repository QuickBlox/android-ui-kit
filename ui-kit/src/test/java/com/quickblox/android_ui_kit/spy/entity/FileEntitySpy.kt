/*
 * Created by Injoit on 13.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity

import android.net.Uri
import com.quickblox.android_ui_kit.stub.entity.FileEntityStub
import java.io.File
import kotlin.random.Random

class FileEntitySpy(private var file: File? = null) : FileEntityStub() {
    override fun getUri(): Uri? {
        return null
    }

    override fun setUri(uri: Uri?) {

    }

    override fun getFile(): File? {
        return file
    }

    override fun setFile(file: File?) {
        this.file = file
    }

    override fun setUrl(url: String?) {

    }

    override fun getUrl(): String? {
        return ""
    }

    override fun setMimeType(type: String?) {

    }

    override fun getMimeType(): String? {
        return ""
    }

    override fun setUid(uid: String?) {

    }

    override fun getUid(): String? {
       return ""
    }

    override fun getId(): Int? {
        return Random.nextInt(1000, 10000)
    }

    override fun setId(id: Int?) {

    }
}