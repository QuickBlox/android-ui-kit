/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.remote

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.data.repository.file.FilesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class UploadFileTest : BaseTest() {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val remoteDataSource = RemoteDataSourceImpl()
    private val localFileDataSource = LocalFileDataSourceImpl(context)
    private val filesRepository: FilesRepository = FilesRepositoryImpl(remoteDataSource, localFileDataSource)

    @Before
    fun init() {
        initQuickblox()
        loginToRest()
    }

    @After
    fun release() {
        logoutFromRest()
    }

    @Test
    fun createTxtLocalFile_saveFileToRemote_receivedFile() {
        val createdFile = filesRepository.createLocalFile("txt")
        createdFile.getFile()!!.writeBytes("temp_file_${System.currentTimeMillis()}".toByteArray())

        val savedFileEntity = filesRepository.saveFileToRemote(createdFile)
        assertTrue(savedFileEntity.getMimeType()!!.isNotEmpty())
        assertTrue(savedFileEntity.getUrl()!!.isNotEmpty())
    }

    @Test
    fun createPdfLocalFile_saveFileToRemote_receivedFile() {
        val createdFile = filesRepository.createLocalFile("pdf")
        createdFile.getFile()!!.writeBytes("temp_file_${System.currentTimeMillis()}".toByteArray())

        val savedFileEntity = filesRepository.saveFileToRemote(createdFile)
        assertTrue(savedFileEntity.getMimeType()!!.isNotEmpty())
        assertTrue(savedFileEntity.getUrl()!!.isNotEmpty())
    }
}