/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.file

import android.net.Uri
import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.repository.mapper.FileMapper
import com.quickblox.android_ui_kit.data.source.exception.LocalFileDataSourceException
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException
import com.quickblox.android_ui_kit.domain.repository.FilesRepository

class FilesRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localFileDataSource: LocalFileDataSource
) : FilesRepository {
    private val exceptionFactory: FilesRepositoryExceptionFactory = FilesRepositoryExceptionFactoryImpl()

    override fun saveFileToLocal(entity: FileEntity) {
        try {
            val dto = FileMapper.fromEntityToLocal(entity)
            localFileDataSource.createFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun saveFileToRemote(entity: FileEntity): FileEntity {
        try {
            val dto = FileMapper.fromEntityToRemote(entity)
            val remoteDTO = remoteDataSource.createFile(dto)
            return FileMapper.toEntity(remoteDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getFileFromLocal(id: String): FileEntity {
        try {
            val dto = FileMapper.localDTOFrom(id)
            val localDTO = localFileDataSource.getFile(dto)
            return FileMapper.toEntity(localDTO)
        } catch (exception: LocalFileDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getFileFromRemote(id: String): FileEntity {
        try {
            val dto = FileMapper.remoteDTOFrom(id)
            val remoteDTO = remoteDataSource.getFile(dto)
            return FileMapper.toEntity(remoteDTO)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deleteFileFromLocal(id: String) {
        try {
            val dto = FileMapper.localDTOFrom(id)
            localFileDataSource.deleteFile(dto)
        } catch (exception: LocalFileDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun deleteFileFromRemote(id: String) {
        try {
            val dto = FileMapper.remoteDTOFrom(id)
            remoteDataSource.deleteFile(dto)
        } catch (exception: RemoteDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun createLocalFile(extension: String): FileEntity {
        try {
            val createdDTO = localFileDataSource.createFile(extension)
            return FileMapper.toEntity(createdDTO)
        } catch (exception: LocalFileDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }

    override fun getFileFromLocalByUri(uri: Uri): FileEntity {
        try {
            val dto = LocalFileDTO()
            dto.uri = uri

            val loadedDTO = localFileDataSource.getFileByUri(dto)
            return FileMapper.toEntity(loadedDTO)
        } catch (exception: LocalFileDataSourceException) {
            throw exceptionFactory.makeBy(exception.code, exception.description)
        } catch (exception: MappingException) {
            throw exceptionFactory.makeIncorrectData(exception.message.toString())
        }
    }
}