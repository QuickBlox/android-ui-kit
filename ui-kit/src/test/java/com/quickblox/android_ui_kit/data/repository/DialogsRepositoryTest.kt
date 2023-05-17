/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogsDTO
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.repository.dialog.DialogsRepositoryImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.data.source.remote.mapper.RemoteDialogDTOMapper
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.stub.source.LocalDataSourceStub
import com.quickblox.android_ui_kit.stub.source.RemoteDataSourceStub
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.exception.QBResponseException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DialogsRepositoryTest {
    private val localExceptionFactory = LocalDataSourceExceptionFactoryImpl()
    private val remoteExceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    @Test
    @ExperimentalCoroutinesApi
    fun saveDialogToLocal_NoExceptions() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override fun saveDialog(dto: LocalDialogDTO) {
                // empty
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        dialogRepository.saveDialogToLocal(DialogEntitySpy())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun saveDialogToLocal_InCorrectDataException() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override fun saveDialog(dto: LocalDialogDTO) {
                // empty
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        val dialogStub = DialogEntityImpl()
        dialogStub.setDialogType(null)

        try {
            dialogRepository.saveDialogToLocal(dialogStub)
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun saveDialogToLocal_AlreadyExistException() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override fun saveDialog(dto: LocalDialogDTO) {
                throw localExceptionFactory.makeAlreadyExist("Dialog already exist")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        try {
            dialogRepository.saveDialogToLocal(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.ALREADY_EXIST, exception.code)
        }
    }

    @Test
    fun createDialogInRemote_EntityNotNull() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val dialog = QBChatDialog()
                dialog.type = QBDialogType.GROUP
                dialog.name = "Test Dialog"
                return RemoteDialogDTOMapper.toDTOFrom(dialog, null)
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val remoteEntity = dialogRepository.createDialogInRemote(DialogEntitySpy())

        // TODO: Need add assert to check "isValid" all fields in entity
        assertNotNull(remoteEntity)
    }

    @Test
    fun createDialogInRemote_UnexpectedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val badRequestCode = 400
                val errors = arrayListOf<String>()
                errors.add("Bad request")

                val responseException = QBResponseException(badRequestCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.createDialogInRemote(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.UNEXPECTED, exception.code)
        }
    }

    @Test
    fun createDialogInRemote_IncorrectDataException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val unprocessableEntity = 422
                val errors = arrayListOf<String>()
                errors.add("Unprocessable Entity")

                val responseException = QBResponseException(unprocessableEntity, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val dialogStub = DialogEntityImpl()
        dialogStub.setDialogType(null)
        try {
            dialogRepository.createDialogInRemote(dialogStub)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun createDialogInRemote_ForbiddenException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val forbidden = 403
                val errors = arrayListOf<String>()
                errors.add("Forbidden")

                val responseException = QBResponseException(forbidden, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.createDialogInRemote(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.RESTRICTED_ACCESS, exception.code)
        }
    }

    @Test
    fun createDialogInRemote_ConnectionFailedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun createDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val serviceUnavailableCode = 503
                val errors = arrayListOf<String>()
                errors.add("Service unavailable")

                val responseException = QBResponseException(serviceUnavailableCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.createDialogInRemote(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun updateDialogInLocal_NoExceptions() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override suspend fun updateDialog(dto: LocalDialogDTO) {
                // empty
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        dialogRepository.updateDialogInLocal(DialogEntitySpy())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun updateDialogInLocal_NotFoundException() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override suspend fun updateDialog(dto: LocalDialogDTO) {
                throw localExceptionFactory.makeNotFound("Dialog not found")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        try {
            dialogRepository.updateDialogInLocal(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun updateDialogInLocal_IncorrectDataException() = runTest {
        val localDataSourceStub = object : LocalDataSourceStub() {
            override suspend fun updateDialog(dto: LocalDialogDTO) {
                throw localExceptionFactory.makeNotFound("Dialog not found")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSourceStub)
        val dialogStub = DialogEntityImpl()
        dialogStub.setDialogType(null)
        try {
            dialogRepository.updateDialogInLocal(dialogStub)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun updateDialogInRemote_EntityNotNull() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val dialog = QBChatDialog()
                dialog.type = QBDialogType.GROUP
                dialog.name = "Test Dialog"
                return RemoteDialogDTOMapper.toDTOFrom(dialog, null)
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val entity = dialogRepository.updateDialogInRemote(DialogEntitySpy())

        // TODO: Need add assert to check "isValid" all fields in entity
        assertNotNull(entity)
    }

    @Test
    fun updateDialogInRemote_UnexpectedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val badRequestCode = 400
                val errors = arrayListOf<String>()
                errors.add("Bad request")

                val responseException = QBResponseException(badRequestCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.updateDialogInRemote(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.UNEXPECTED, exception.code)
        }
    }

    @Test
    fun updateDialogInRemote_IncorrectDataException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val badRequestCode = 400
                val errors = arrayListOf<String>()
                errors.add("Bad request")

                val responseException = QBResponseException(badRequestCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val dialogStub = DialogEntityImpl()
        dialogStub.setDialogType(null)
        try {
            dialogRepository.updateDialogInRemote(dialogStub)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun updateDialogInRemote_ConnectionFailedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val serviceUnavailableCode = 503
                val errors = arrayListOf<String>()
                errors.add("Service unavailable")

                val responseException = QBResponseException(serviceUnavailableCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.updateDialogInRemote(DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    fun getDialogFromLocal_EntityNotNull() {
        val localDataSource = object : LocalDataSourceStub() {
            override fun getDialog(dto: LocalDialogDTO): LocalDialogDTO {
                val dtoStub = LocalDialogDTO()
                dtoStub.type = 2
                dtoStub.name = "Test Dialog"
                return dtoStub
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        val entity = dialogRepository.getDialogFromLocal("stub_dialog_id")

        // TODO: Need add assert to check "isValid" all fields in entity
        assertNotNull(entity)
    }

    @Test
    fun getDialogFromLocal_NotFoundException() {
        val localDataSource = object : LocalDataSourceStub() {
            override fun getDialog(dto: LocalDialogDTO): LocalDialogDTO {
                throw localExceptionFactory.makeNotFound("Dialog not found")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        try {
            dialogRepository.getDialogFromLocal("stub_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun getDialogFromLocal_IncorrectDataException() {
        val localDataSource = object : LocalDataSourceStub() {
            override fun getDialog(dto: LocalDialogDTO): LocalDialogDTO {
                val dtoStub = LocalDialogDTO()
                dtoStub.type = null
                return dtoStub
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        try {
            dialogRepository.getDialogFromLocal("stub_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun getDialogFromRemote_EntityNotNull() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val dialog = QBChatDialog()
                dialog.type = QBDialogType.GROUP
                dialog.name = "Test Dialog"
                return RemoteDialogDTOMapper.toDTOFrom(dialog, null)
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val entity = dialogRepository.getDialogFromRemote("stub_dialog_id")

        // TODO: Need add assert to check "isValid" all fields in entity
        assertNotNull(entity)
    }

    @Test
    fun getDialogFromRemote_NotFoundException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val notFoundCode = 404
                val errors = arrayListOf<String>()
                errors.add("Not found")

                val responseException = QBResponseException(notFoundCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.getDialogFromRemote("stub_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun getDialogFromRemote_IncorrectDataException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val dto = RemoteDialogDTO()
                dto.type = null
                return dto
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.getDialogFromRemote("stub_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }

    @Test
    fun getDialogFromRemote_UnauthorisedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getDialog(dto: RemoteDialogDTO): RemoteDialogDTO {
                val unauthorizedCode = 401
                val errors = arrayListOf<String>()
                errors.add("Unauthorized")

                val responseException = QBResponseException(unauthorizedCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        try {
            dialogRepository.getDialogFromRemote("stub_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.UNAUTHORISED, exception.code)
        }
    }

    @Test
    fun getAllDialogsFromLocal_EntitiesNotEmpty() {
        val localDataSource = object : LocalDataSourceStub() {
            override fun getDialogs(): LocalDialogsDTO {
                val dialogsDTO = LocalDialogsDTO()
                val dto = LocalDialogDTO()
                dto.type = 2
                dto.name = "Test Dialog"
                dialogsDTO.dialogs = arrayListOf(dto)
                return dialogsDTO
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        val entities: Collection<DialogEntity> = dialogRepository.getAllDialogsFromLocal()

        assertTrue(entities.isNotEmpty())
    }

    @Test
    fun getAllDialogsFromLocal_UnexpectedException() {
        val localDataSource = object : LocalDataSourceStub() {
            override fun getDialogs(): LocalDialogsDTO {
                throw localExceptionFactory.makeUnexpected("Unexpected")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        try {
            dialogRepository.getAllDialogsFromLocal()
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.UNEXPECTED, exception.code)
        }
    }

    @Test
    fun getDialogsFromRemote_EntityExist_resultSuccess() = runTest {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getAllDialogs(): Flow<Result<RemoteDialogDTO>> {
                return flow {
                    val dialogDTO = RemoteDialogDTO()
                    dialogDTO.type = DialogEntity.Types.PRIVATE.code
                    dialogDTO.name = "Test Dialog"
                    dialogDTO.participantIds = arrayListOf(12222, 13333)

                    emit(Result.success(dialogDTO))
                }
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        dialogRepository.getAllDialogsFromRemote().collect { result ->
            assertTrue(result.isSuccess)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getDialogsFromRemote_UnauthorisedException_resultFailure() = runTest {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun getAllDialogs(): Flow<Result<RemoteDialogDTO>> {
                return flow {
                    val unauthorizedCode = 401
                    val errors = arrayListOf<String>()
                    errors.add("Unauthorized")

                    val responseException = QBResponseException(unauthorizedCode, errors)
                    val errorMessage = responseException.message.toString()
                    val exception = remoteExceptionFactory.makeBy(responseException.httpStatusCode, errorMessage)

                    emit(Result.failure(exception))
                }
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})

        dialogRepository.getAllDialogsFromRemote().collect { result ->
            assertTrue(result.isFailure)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun deleteDialogFromLocal_NoExceptions() = runTest {
        val localDataSource = object : LocalDataSourceStub() {
            override fun deleteDialog(dto: LocalDialogDTO) {
                // empty
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)

        dialogRepository.deleteDialogFromLocal("stub_dialog_id")
    }

    @Test
    @ExperimentalCoroutinesApi
    fun deleteDialogFromLocal_NotFoundException() = runTest {
        val localDataSource = object : LocalDataSourceStub() {
            override fun deleteDialog(dto: LocalDialogDTO) {
                throw localExceptionFactory.makeNotFound("Dialog not found")
            }
        }
        val dialogRepository = DialogsRepositoryImpl(object : RemoteDataSourceStub() {}, localDataSource)
        try {
            dialogRepository.deleteDialogFromLocal("not_exist_dialog_id")
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun deleteDialogFromRemote_NoExceptions() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun leaveDialog(dto: RemoteDialogDTO) {
                // empty
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val entity = object : DialogEntitySpy() {
            override fun getDialogId(): String {
                return "Test dialogId"
            }
        }
        dialogRepository.leaveDialogFromRemote(entity)
    }

    @Test
    fun deleteDialogFromRemote_NotFoundException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun leaveDialog(dto: RemoteDialogDTO) {
                val notFoundCode = 404
                val errors = arrayListOf<String>()
                errors.add("Not found")

                val responseException = QBResponseException(notFoundCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode, responseException.message.toString()
                )
            }
        }
        val dialogRepository = DialogsRepositoryImpl(remoteDataSource, object : LocalDataSourceStub() {})
        val entity = object : DialogEntitySpy() {
            override fun getDialogId(): String {
                return "Test dialogId"
            }
        }
        try {
            dialogRepository.leaveDialogFromRemote(entity)
            fail("expect: Exception, actual: No Exception")
        } catch (exception: DialogsRepositoryException) {
            assertEquals(DialogsRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }
}