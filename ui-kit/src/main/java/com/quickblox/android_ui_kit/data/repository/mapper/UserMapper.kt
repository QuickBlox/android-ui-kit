/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.local.user.LocalUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUsersDTO
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.UserEntityImpl

object UserMapper {
    fun fromEntityLocal(entity: UserEntity): LocalUserDTO {
        val dto = LocalUserDTO()
        dto.id = entity.getUserId()
        return dto
    }

    fun toEntity(dto: LocalUserDTO): UserEntity {
        val userId = dto.id
        //TODO: Need add logic for handle LocalUserDTO
        return UserEntityImpl()
    }

    fun toEntity(dtos: Collection<RemoteUserDTO>): Collection<UserEntity> {
        val entities = arrayListOf<UserEntity>()

        dtos.forEach { userDto ->
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

    fun localDToFrom(userId: Int): LocalUserDTO {
        val dto = LocalUserDTO()
        dto.id = userId
        return dto
    }

    fun remoteDTOFrom(userId: Int): RemoteUserDTO {
        val dto = RemoteUserDTO()
        dto.id = userId
        return dto
    }

    fun remoteUsersDTOFrom(userIds: ArrayList<Int>): RemoteUsersDTO {
        val usersDTO = arrayListOf<RemoteUserDTO>()

        userIds.forEach { id ->
            val dto = remoteDTOFrom(id)
            usersDTO.add(dto)
        }
        val dto = RemoteUsersDTO()
        dto.users = usersDTO
        return dto
    }

    fun getEntitiesFrom(dto: RemoteUsersDTO): List<UserEntity> {
        val entities = arrayListOf<UserEntity>()
        dto.users.forEach {
            val entity = toEntity(it)
            entities.add(entity)
        }
        return entities
    }
}