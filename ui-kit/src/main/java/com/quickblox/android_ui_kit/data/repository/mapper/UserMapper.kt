/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl

object UserMapper {
    fun toEntity(dtos: Collection<RemoteUserDTO>): Collection<UserEntity> {
        val entities = arrayListOf<UserEntity>()

        for (userDto in dtos) {
            val userEntity = toEntity(userDto)
            entities.add(userEntity)
        }

        return entities
    }

    fun toEntity(dto: RemoteUserDTO): UserEntity {
        val entity = UserEntityImpl()
        entity.setUserId(dto.id)
        entity.setName(dto.name)
        entity.setEmail(dto.email)
        entity.setLogin(dto.login)
        entity.setPhone(dto.phone)
        entity.setWebsite(dto.website)
        entity.setLastRequestAt(dto.lastRequestAt)
        entity.setExternalId(dto.externalId)
        entity.setFacebookId(dto.facebookId)
        entity.setAvatarUrl(dto.avatarUrl)
        entity.setTags(dto.tags)
        entity.setCustomData(dto.customData)
        return entity
    }

    fun remoteDTOFrom(userId: Int): RemoteUserDTO {
        val dto = RemoteUserDTO()
        dto.id = userId
        return dto
    }
}