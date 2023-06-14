/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import android.net.Uri
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.File
import kotlin.random.Random

class FileEntityTest {
    @Test
    fun buildFileEntity_setFields_fieldsAreEquals() {
        val file = File("testPathName")
        val id = Random.nextInt(100, 1000)
        val uri = Uri.fromFile(file)
        val url = System.currentTimeMillis().toString()
        val mimeType = System.currentTimeMillis().toString()

        val entity = FileEntityImpl()
        entity.setFile(file)
        entity.setId(id)
        entity.setUri(uri)
        entity.setUrl(url)
        entity.setMimeType(mimeType)

        assertEquals(file, entity.getFile())
        assertEquals(id, entity.getId())
        assertEquals(uri, entity.getUri())
        assertEquals(url, entity.getUrl())
        assertEquals(mimeType, entity.getMimeType())

        file.delete()
    }

    @Test
    fun buildFileEntityWithMimeTypeAudio_getFileType_fileTypeIsAudio() {
        val entity = FileEntityImpl()
        entity.setMimeType("audio/aac")

        assertEquals(FileEntity.FileTypes.AUDIO, entity.getFileType())
    }

    @Test
    fun buildFileEntityWithMimeTypeApplication_getFileType_fileTypeIsFile() {
        val entity = FileEntityImpl()
        entity.setMimeType("application/gzip")

        assertEquals(FileEntity.FileTypes.FILE, entity.getFileType())
    }

    @Test
    fun buildFileEntityWithMimeTypeImage_getFileType_fileTypeIsImage() {
        val entity = FileEntityImpl()
        entity.setMimeType("image/gif")

        assertEquals(FileEntity.FileTypes.IMAGE, entity.getFileType())
    }

    @Test
    fun buildFileEntityWithMimeTypeVideo_getFileType_fileTypeIsVideo() {
        val entity = FileEntityImpl()
        entity.setMimeType("video/x-msvideo")

        assertEquals(FileEntity.FileTypes.VIDEO, entity.getFileType())
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildFileEntityWithMimeNotExist_getFileType_receivedException() {
        val entity = FileEntityImpl()
        entity.setMimeType("not_exist/mimeType")
        entity.getFileType()
    }
}