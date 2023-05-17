/*
 * Created by Injoit on 08.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException.Codes.*
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class LocalFileDataSourceTest {
    private var localFileDataSource: LocalFileDataSource? = null

    @Before
    fun createLocalFileDataSource() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        localFileDataSource = LocalFileDataSourceImpl(appContext)
    }

    @After
    fun clearAllAndReleaseLocalFileDataSource() {
        try {
            localFileDataSource?.clearAll()
            localFileDataSource = null
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(RESTRICTED_ACCESS, exception.code)
        }
    }

    @Test
    fun createFile_getFile_EqualsFiles() {
        val createdDTO = createRandomLocalFileDTO()
        localFileDataSource?.createFile(createdDTO)

        val loadedDTO = localFileDataSource?.getFile(createdDTO)

        val isSameId = createdDTO.id == loadedDTO?.id
        val isSameType = createdDTO.type == loadedDTO?.type
        val isSameName = createdDTO.name == loadedDTO?.name
        val isSameData = createdDTO.data.contentEquals(loadedDTO?.data)
        assertTrue(isSameId && isSameType && isSameName && isSameData)
    }

    private fun createRandomLocalFileDTO(): LocalFileDTO {
        val dto = LocalFileDTO()
        val randomId = Random.nextInt(10000000, 100000000)
        val randomNamePrefix = Random.nextInt(1, 100)

        dto.id = randomId.toString()
        dto.name = "${randomNamePrefix}test.txt "
        dto.type = "image"
        dto.data = "Test bytes".toByteArray()

        return dto
    }

    private fun createDefaultLocalFileDTO(): LocalFileDTO {
        val dto = LocalFileDTO()
        dto.id = "123456"
        dto.name = "test.txt "
        dto.type = "image"
        dto.data = "Test bytes".toByteArray()

        return dto
    }

    @Test
    fun createDTO_createTwoIdenticalFiles_ThrowAlreadyExistException() {
        val dto = createDefaultLocalFileDTO()
        try {
            localFileDataSource?.createFile(dto)
            localFileDataSource?.createFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(ALREADY_EXIST, exception.code)
        }
    }

    @Test
    fun createEmptyDTO_createFile_ThrowIncorrectDataException() {
        val dto = LocalFileDTO()
        try {
            localFileDataSource?.createFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun createDTO_getFile_ThrowNotFoundException() {
        val dto = createDefaultLocalFileDTO()
        try {
            localFileDataSource?.getFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun createEmptyDTO_getFile_ThrowIncorrectDataException() {
        val dto = LocalFileDTO()
        try {
            localFileDataSource?.getFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun createEmptyDTO_deleteFile_ThrowIncorrectDataException() {
        val dto = LocalFileDTO()
        try {
            localFileDataSource?.deleteFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            assertEquals(INCORRECT_DATA, exception.code)
        }
    }
}