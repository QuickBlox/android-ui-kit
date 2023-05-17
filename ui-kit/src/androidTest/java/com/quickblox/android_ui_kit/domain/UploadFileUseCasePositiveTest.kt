/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.file.FilesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.domain.usecases.UploadFileUseCase
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UploadFileUseCasePositiveTest : BaseTest() {
    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val filesRepository = FilesRepositoryImpl(RemoteDataSourceImpl(), LocalFileDataSourceImpl(context))

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getFilesRepository(): FilesRepository {
                return filesRepository
            }
        })
    }

    @After
    fun release() {
        logoutFromRest()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun correctFileRepository_execute_existFile() = runBlocking {
        var uploadedFile: FileEntity? = null

        withContext(Dispatchers.Main) {
            runCatching {
                UploadFileUseCase(buildFileEntity()).execute()
            }.onSuccess { result ->
                uploadedFile = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        assertTrue(uploadedFile?.getUrl()!!.isNotEmpty())
    }

    private fun buildFileEntity(): FileEntity {
        val entity = FileEntityImpl()
        entity.setFile(buildFile())

        return entity
    }

    private fun buildFile(): File {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

        val name = generateName()
        val file = File(context.cacheDir, name)

        val text = "Hello world from Android UI Kit test!"
        file.writeBytes(text.toByteArray())

        return file
    }

    private fun generateName(): String {
        return "${System.currentTimeMillis()}_temp_file.txt"
    }
}