/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.FilesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.FileEntitySpy
import com.quickblox.android_ui_kit.spy.repository.FileRepositorySpy
import com.quickblox.android_ui_kit.stub.DependencyStub
import com.quickblox.android_ui_kit.utils.FileUtils
import junit.framework.Assert.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import java.io.File

class UploadFileUseCaseTest : BaseTest() {
    @Test
    fun fileHasNULLFile_isNotCorrectFile_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())
        val isNotCorrectFile = UploadFileUseCase(FileEntitySpy()).isNotCorrectFile(null)
        assertTrue(isNotCorrectFile)
    }

    @Test
    fun fileHasEmptyLength_isNotCorrectFile_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = File("temp")
        val isNotCorrectFile = UploadFileUseCase(FileEntitySpy()).isNotCorrectFile(file)
        assertTrue(isNotCorrectFile)
    }

    @Test
    fun existCorrectFile_isNotCorrectFile_receivedFalse() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = File("temp")
        file.writeBytes("Test file bytes".toByteArray())

        val isNotCorrectFile = UploadFileUseCase(FileEntitySpy()).isNotCorrectFile(file)

        file.delete()
        assertFalse(isNotCorrectFile)
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun fileEntityHasNULLFile_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        UploadFileUseCase(FileEntitySpy()).execute()
    }

    @Test
    fun fileSizeLess10MB_isNotCorrectSize_receivedFalse() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = File("temp")
        file.writeBytes("Test file bytes".toByteArray())

        val isNotCorrectFile = UploadFileUseCase(FileEntitySpy()).isNotCorrectSize(file, 10)

        file.delete()
        assertFalse(isNotCorrectFile)
    }

    @Test
    fun fileSizeMore10MB_isNotCorrectSize_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = FileUtils.buildFileWithMegaBytesLength(11)

        val isNotCorrectFile = UploadFileUseCase(FileEntitySpy()).isNotCorrectSize(file, 10)

        file.delete()
        assertTrue(isNotCorrectFile)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun fileEntityHas11MBSize_execute_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val file = FileUtils.buildFileWithMegaBytesLength(11)

        val fileEntity = FileEntitySpy()
        fileEntity.setFile(file)

        try {
            UploadFileUseCase(fileEntity).execute()
            fail("expected: Exception, actual: NoException")
        } catch (exception: DomainException) {
            file.delete()
            assertNotNull(exception)
        }
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun saveFileToRemoteThrowException_execute_receivedException() = runTest {
        val fileRepository = object : FileRepositorySpy() {
            override fun saveFileToRemote(entity: FileEntity): FileEntity {
                throw FilesRepositoryException(FilesRepositoryException.Codes.CONNECTION_FAILED, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencyStub() {
            override fun getFilesRepository(): FilesRepository {
                return fileRepository
            }
        })

        UploadFileUseCase(FileEntitySpy()).execute()
    }

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

        val file = File("temp")
        file.writeBytes("Test file bytes".toByteArray())
        createdFile?.setFile(file)

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

        file.delete()

        assertTrue(uploadedFile?.getUrl()!!.isNotEmpty())
    }
}