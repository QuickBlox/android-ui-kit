/*
 * Created by Injoit on 16.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.FileEntityImpl
import com.quickblox.android_ui_kit.domain.exception.repository.MappingException

object FileMapper {
    fun fromEntityToLocal(entity: FileEntity): LocalFileDTO {
        val dto = LocalFileDTO()
        return dto
    }

    fun fromEntityToRemote(entity: FileEntity): RemoteFileDTO {
        if (entity.getFile() == null) {
            throw MappingException("File can't be null")
        }

        val dto = RemoteFileDTO()
        dto.file = entity.getFile()
        dto.mimeType = entity.getMimeType()
        dto.url = entity.getUrl()
        dto.uri = entity.getUri()
        dto.id = entity.getId()

        return dto
    }

    fun toEntity(dto: RemoteFileDTO): FileEntity {
        if (dto.url?.isEmpty() == true) {
            throw MappingException("Url can't be null")
        }

        val entity = FileEntityImpl()
        entity.setFile(dto.file)
        entity.setUrl(dto.url)
        entity.setMimeType(dto.mimeType)
        entity.setUri(dto.uri)
        entity.setId(dto.id)

        return entity
    }

    fun toEntity(dto: LocalFileDTO): FileEntity {
        if (dto.uri == null) {
            throw MappingException("URI can't be null")
        }
        if (dto.file == null) {
            throw MappingException("File can't be null")
        }

        val entity = FileEntityImpl()
        entity.setUri(dto.uri)
        entity.setFile(dto.file)
        entity.setMimeType(dto.mimeType)
        entity.setUrl(dto.url)

        return entity
    }

    fun localDTOFrom(fileId: String): LocalFileDTO {
        val dto = LocalFileDTO()
        dto.id = fileId
        return dto
    }

    fun remoteDTOFrom(fileId: String): RemoteFileDTO {
        val dto = RemoteFileDTO()
        dto.id = fileId.toInt()
        return dto
    }
}