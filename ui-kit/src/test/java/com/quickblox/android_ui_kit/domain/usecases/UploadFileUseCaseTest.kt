/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.spy.repository.FileRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

class UploadFileUseCaseTest : BaseTest() {
    @Test
    @ExperimentalCoroutinesApi
    fun repositoryHasFileEntity_execute_entityExist() = runTest {
        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getFilesRepository(): FilesRepository {
                return FileRepositorySpy()
            }
        })

        var createdFile: FileEntity? = null

        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                CreateLocalFileUseCase("jpg").execute()
            }.onSuccess { result ->
                createdFile = result
            }.onFailure { error ->
                fail("expected: onSuccess, actual: Exception, details $error")
            }
        }

        var uploadedFile: FileEntity? = null

        withContext(UnconfinedTestDispatcher()) {
            runCatching {
                UploadFileUseCase(createdFile!!).execute()
            }.onSuccess { result ->
                uploadedFile = result
            }.onFailure { error ->
                fail("expected: onSuccess, actual: Exception, details $error")
            }
        }

        assertTrue(uploadedFile?.getUrl()!!.isNotEmpty())
    }
}