/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.android_ui_kit.data.repository.file.FilesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.domain.exception.repository.FilesRepositoryException
import com.quickblox.android_ui_kit.spy.entity.FileEntitySpy
import com.quickblox.android_ui_kit.stub.source.LocalFileDataSourceStub
import com.quickblox.android_ui_kit.stub.source.RemoteDataSourceStub
import com.quickblox.android_ui_kit.stub.entity.FileEntityStub
import com.quickblox.core.exception.QBResponseException
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FilesRepositoryTest {
    private val localFileExceptionFactory = LocalFileDataSourceExceptionFactoryImpl()
    private val remoteFileExceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    @Test
    fun saveFileToLocal_NoException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun createFile(dto: LocalFileDTO) {
                // empty
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        val entity = FileEntityStub()

        fileRepository.saveFileToLocal(entity)
    }

    @Test
    fun saveFileToLocal_UnexpectedException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun createFile(dto: LocalFileDTO) {
                throw localFileExceptionFactory.makeUnexpected("Unexpected")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            val entity = FileEntityStub()
            fileRepository.saveFileToLocal(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.UNEXPECTED, exception.code)
        }
    }

    @Test
    fun saveFileToLocal_IncorrectDataException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun createFile(dto: LocalFileDTO) {
                throw localFileExceptionFactory.makeIncorrectData("Incorrect Data")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            val entity = FileEntityStub()
            fileRepository.saveFileToLocal(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun saveFileToLocal_AlreadyExistException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun createFile(dto: LocalFileDTO) {
                throw localFileExceptionFactory.makeAlreadyExist("Already exist")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            val entity = FileEntityStub()
            fileRepository.saveFileToLocal(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.ALREADY_EXIST, exception.code)
        }
    }

    @Test
    fun saveFileToRemote_NoException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
                val dtoStub = RemoteFileDTO()
                dtoStub.id = 123456
                dtoStub.file = File("test")
                return dtoStub
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        val entity = FileEntitySpy()
        val dto = fileRepository.saveFileToRemote(entity)
        assertNotNull(dto)
    }

    @Test
    fun saveFileToRemote_AlreadyExistException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
                val tooManyRequests = 429
                val errors = arrayListOf<String>()
                errors.add("Too Many Requests")

                val responseException = QBResponseException(tooManyRequests, errors)
                throw remoteFileExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            val entity = FileEntitySpy()
            fileRepository.saveFileToRemote(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.RESTRICTED_ACCESS, exception.code)
        }
    }

    @Test
    fun saveFileToRemote_IncorrectDataException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
                throw remoteFileExceptionFactory.makeIncorrectData("")
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            val entity = FileEntitySpy()
            fileRepository.saveFileToRemote(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun saveFileToRemote_InternalServerException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
                val internalServerError = 500
                val errors = arrayListOf<String>()
                errors.add("Internal Server Error")

                val responseException = QBResponseException(internalServerError, errors)
                throw remoteFileExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            val entity = FileEntitySpy()
            fileRepository.saveFileToRemote(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    fun getFileFromLocal_NotFoundException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun getFile(dto: LocalFileDTO): LocalFileDTO {
                throw localFileExceptionFactory.makeNotFound("Not Found")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            fileRepository.getFileFromLocal("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun getFileFromLocal_WriteAndReadException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun getFile(dto: LocalFileDTO): LocalFileDTO {
                throw localFileExceptionFactory.makeWriteAndRead("Write And Read")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            fileRepository.getFileFromLocal("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.WRITE_AND_READ, exception.code)
        }
    }

    @Test
    fun getFileFromLocal_IncorrectDataException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun getFile(dto: LocalFileDTO): LocalFileDTO {
                val dtoStub = LocalFileDTO()
                dtoStub.id = null
                return dtoStub
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            fileRepository.getFileFromLocal("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun getFileFromLocal_RestrictedAccessException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun getFile(dto: LocalFileDTO): LocalFileDTO {
                throw localFileExceptionFactory.makeRestrictedAccess("Restricted Access")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            fileRepository.getFileFromLocal("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.RESTRICTED_ACCESS, exception.code)
        }
    }

    @Test
    fun getFileFromRemote_NoException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun getFile(dto: RemoteFileDTO): RemoteFileDTO {
                val dtoStub = RemoteFileDTO()
                dtoStub.id = 123456
                return dtoStub
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        val dto = fileRepository.getFileFromRemote("123456")

        assertNotNull(dto)
    }

    @Test
    fun getFileFromRemote_IncorrectDataException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun getFile(dto: RemoteFileDTO): RemoteFileDTO {
                throw remoteFileExceptionFactory.makeIncorrectData("")
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})

        try {
            fileRepository.getFileFromRemote("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun getFileFromRemote_UnProcessableEntityException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun getFile(dto: RemoteFileDTO): RemoteFileDTO {
                val unProcessableEntity = 422
                val errors = arrayListOf<String>()
                errors.add("Un Processable Entity")

                val responseException = QBResponseException(unProcessableEntity, errors)
                throw remoteFileExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            fileRepository.getFileFromRemote("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun getFileFromRemote_UnexpectedException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun createFile(dto: RemoteFileDTO): RemoteFileDTO {
                val internalServerError = 12345
                val errors = arrayListOf<String>()
                errors.add("Error")

                val responseException = QBResponseException(internalServerError, errors)
                throw remoteFileExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            val entity = FileEntitySpy()
            fileRepository.saveFileToRemote(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.UNEXPECTED, exception.code)
        }
    }

    @Test
    fun deleteFileFromLocal_NoException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun deleteFile(dto: LocalFileDTO) {
                // empty
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        fileRepository.deleteFileFromLocal("123456")
    }

    @Test
    fun deleteFileFromLocal_NotFoundException() {
        val localFileDataSourceStub = object : LocalFileDataSourceStub() {
            override fun deleteFile(dto: LocalFileDTO) {
                throw localFileExceptionFactory.makeNotFound("Not Found")
            }
        }
        val fileRepository = FilesRepositoryImpl(object : RemoteDataSourceStub() {}, localFileDataSourceStub)
        try {
            fileRepository.deleteFileFromLocal("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun deleteFileFromRemote_NoException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun deleteFile(dto: RemoteFileDTO) {
                // empty
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        val dto = fileRepository.deleteFileFromRemote("123456")
        assertNotNull(dto)
    }

    @Test
    fun deleteFileFromRemote_NotFoundException() {
        val remoteDataSourceStub = object : RemoteDataSourceStub() {
            override fun deleteFile(dto: RemoteFileDTO) {
                val notFound = 404
                val errors = arrayListOf<String>()
                errors.add("Not Found")

                val responseException = QBResponseException(notFound, errors)
                throw remoteFileExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val fileRepository = FilesRepositoryImpl(remoteDataSourceStub, object : LocalFileDataSourceStub() {})
        try {
            fileRepository.deleteFileFromRemote("123456")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: FilesRepositoryException) {
            assertEquals(FilesRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }
}