/*
 * Created by Injoit on 13.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity

import android.net.Uri
import com.quickblox.android_ui_kit.stub.entity.FileEntityStub
import java.io.File
import kotlin.random.Random

class FileEntitySpy : FileEntityStub() {
    override fun getUri(): Uri? {
        return null
    }

    override fun setUri(uri: Uri?) {

    }

    override fun getFile(): File? {
        return buildFile()
    }

    override fun setFile(file: File?) {

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

    private fun buildFile(): File {
        val name = generateName()
        val file = File("test")

        val text = "Hello world from Android UI Kit test!"
        file.writeBytes(text.toByteArray())

        return file
    }

    private fun generateName(): String {
        return "${System.currentTimeMillis()}_temp_file.txt"
    }

    override fun getId(): Int? {
        return Random.nextInt(1000, 10000)
    }

    override fun setId(id: Int?) {

    }
}