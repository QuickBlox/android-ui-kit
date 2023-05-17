/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.dialog

import com.quickblox.android_ui_kit.data.dto.local.dialog.LocalDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.repository.mapper.DialogMapper
import com.quickblox.android_ui_kit.data.source.exception.LocalDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

open class DialogsRepositoryImpl(
    private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource
) : DialogsRepository {
    private val exceptionFactory: DialogsRepositoryExceptionFactory = DialogsRepositoryExceptionFactoryImpl()

    override suspend fun saveDialogToLocal(entity: DialogEntity) {
        try {
            val dto = DialogMapper.dtoLocalFrom(entity)
            localDataSource.saveDialog(dto)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createDialogInRemote(entity: DialogEntity): DialogEntity {
        try {
            val dto = DialogMapper.dtoRemoteFrom(entity)
            val remoteDTO = remoteDataSource.createDialog(dto)
            return DialogMapper.toEntity(remoteDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.message.toString())
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override suspend fun updateDialogInLocal(entity: DialogEntity) {
        try {
            val dto = DialogMapper.dtoLocalFrom(entity)
            localDataSource.updateDialog(dto)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun updateDialogInRemote(entity: DialogEntity): DialogEntity {
        try {
            val dto = DialogMapper.dtoRemoteFrom(entity)
            val remoteDTO = remoteDataSource.updateDialog(dto)
            return DialogMapper.toEntity(remoteDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getDialogFromLocal(dialogId: String): DialogEntity {
        try {
            val dto = DialogMapper.localDTOFrom(dialogId)
            val remoteDTO = localDataSource.getDialog(dto)
            return DialogMapper.toEntity(remoteDTO)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getDialogFromRemote(dialogId: String): DialogEntity {
        try {
            val dto = DialogMapper.remoteDTOFrom(dialogId)
            val dialog = remoteDataSource.getDialog(dto)
            return DialogMapper.toEntity(dialog)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getAllDialogsFromLocal(): Collection<DialogEntity> {
        try {
            val dto = localDataSource.getDialogs()
            return DialogMapper.entitiesFrom(dto)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getAllDialogsFromRemote(): Flow<Result<DialogEntity>> {
        return channelFlow {
            remoteDataSource.getAllDialogs().onEach { result ->
                try {
                    val remoteDialogDTO = result.getOrThrow()
                    val dialogEntity = DialogMapper.toEntity(remoteDialogDTO)
                    send(Result.success(dialogEntity))
                } catch (dataSourceException: RemoteDataSourceException) {
                    val errorMessage = dataSourceException.message.toString()
                    send(Result.failure(exceptionFactory.makeIncorrectData(errorMessage)))
                } catch (mappingException: MappingException) {
                    val errorMessage = mappingException.message.toString()
                    send(Result.failure(exceptionFactory.makeIncorrectData(errorMessage)))
                }
            }.collect()
        }
    }

    override fun getDialogsByName(name: String): Collection<DialogEntity> {
        try {
            val dto = localDataSource.getDialogsByName(name)
            return DialogMapper.entitiesFrom(dto)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun addUsersToDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
        try {
            val dto = DialogMapper.dtoRemoteFrom(entity)
            val dialog = remoteDataSource.addUsersToDialog(dto, userIds)
            return DialogMapper.toEntity(dialog)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun removeUsersFromDialog(entity: DialogEntity, userIds: Collection<Int>): DialogEntity {
        try {
            val dto = DialogMapper.dtoRemoteFrom(entity)
            val dialog = remoteDataSource.removeUsersFromDialog(dto, userIds)
            return DialogMapper.toEntity(dialog)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override suspend fun deleteDialogFromLocal(dialogId: String) {
        try {
            val dto = DialogMapper.localDTOFrom(dialogId)
            localDataSource.deleteDialog(dto)
        } catch (exception: LocalDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun subscribeLocalSaveDialogs(): Flow<DialogEntity?> {
        return localDataSource.subscribeLocalSaveDialogs().map { localDialogDTO ->
            localDialogDTO?.let {
                DialogMapper.toEntity(localDialogDTO)
            }
        }
    }

    override fun leaveDialogFromRemote(entity: DialogEntity) {
        try {
            val dto = DialogMapper.dtoRemoteFrom(entity)
            remoteDataSource.leaveDialog(dto)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun subscribeLocalSyncing(): Flow<Boolean> {
        return localDataSource.subscribeLocalSyncing()
    }

    @ExperimentalCoroutinesApi
    override fun subscribeDialogEvents(): Flow<DialogEntity?> {
        return remoteDataSource.subscribeDialogsEvent().map { remoteDialogDTO ->
            val dialogId = remoteDialogDTO?.id ?: ""

            val dialogDTOFromRemote = getDialogFromRemoteBy(dialogId)
            dialogDTOFromRemote?.let {
                val dialogDTOFromLocal = getDialogFromLocalBy(dialogId)
                if (dialogDTOFromLocal != null) {
                    deleteDialogInLocal(dialogDTOFromLocal)
                }

                val localDialogDTO = DialogMapper.dtoLocalFrom(dialogDTOFromRemote)
                saveDialogInLocal(localDialogDTO)

                DialogMapper.toEntity(dialogDTOFromRemote)
            }
        }
    }

    private fun saveDialogInLocal(dialogDTO: LocalDialogDTO) {
        try {
            localDataSource.saveDialog(dialogDTO)
        } catch (exception: LocalDataSourceException) {
            //todo: need add exception handling
        }
    }

    private fun deleteDialogInLocal(dialogDTO: LocalDialogDTO) {
        try {
            localDataSource.deleteDialog(dialogDTO)
        } catch (exception: LocalDataSourceException) {
            //todo: need add exception handling
        }
    }

    private fun getDialogFromLocalBy(id: String): LocalDialogDTO? {
        val localDialogDTO = LocalDialogDTO()
        localDialogDTO.id = id
        return try {
            localDataSource.getDialog(localDialogDTO)
        } catch (exception: LocalDataSourceException) {
            null
        }
    }

    private fun getDialogFromRemoteBy(id: String): RemoteDialogDTO? {
        val remoteDialogDTO = RemoteDialogDTO()
        remoteDialogDTO.id = id
        return try {
            remoteDataSource.getDialog(remoteDialogDTO)
        } catch (exception: RemoteDataSourceException) {
            null
        }
    }

    override suspend fun setLocalSynced(synced: Boolean) {
        localDataSource.setLocalSynced(synced)
    }

    override fun clearAllDialogsInLocal() {
        localDataSource.clearAll()
    }
}