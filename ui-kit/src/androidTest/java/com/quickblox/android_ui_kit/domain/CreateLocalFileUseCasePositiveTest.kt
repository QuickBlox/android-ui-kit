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
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.domain.usecases.CreateLocalFileUseCase
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateLocalFileUseCasePositiveTest : BaseTest() {
    @Before
    fun init() {
        initDependency()
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

    @Test
    @ExperimentalCoroutinesApi
    fun correctFileRepository_execute_existFile() = runBlocking {
        var createdFile: FileEntity? = null

        withContext(Dispatchers.Main) {
            runCatching {
                CreateLocalFileUseCase("jpg").execute()
            }.onSuccess { result ->
                createdFile = result
            }.onFailure { error ->
                fail("expected: Exception, actual: NotException")
            }
        }

        assertTrue(createdFile?.getUri() != null)
        assertTrue(createdFile?.getFile() != null)
    }
}