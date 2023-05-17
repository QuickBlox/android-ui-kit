/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote.mapper

import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUsersDTO
import com.quickblox.users.model.QBUser

object RemoteUserDTOMapper {
    fun toDTOFrom(users: ArrayList<QBUser>): RemoteUsersDTO {
        val usersDTO = usersDTOFrom(users)
        val dto = RemoteUsersDTO()
        dto.users = usersDTO
        return dto
    }

    fun toDTOFrom(user: QBUser?): RemoteUserDTO {
        return userDTOFrom(user)
    }

    fun getUserIdFrom(remoteUserDTO: RemoteUserDTO): Int? {
        return remoteUserDTO.id
    }

    fun getUserIdsFrom(remoteUsersDTO: RemoteUsersDTO): ArrayList<Int?> {
        val users = remoteUsersDTO.users
        val userIds = arrayListOf<Int?>()
        users.forEach { user ->
            userIds.add(user.id)
        }
        return userIds
    }

    private fun usersDTOFrom(users: ArrayList<QBUser>): ArrayList<RemoteUserDTO> {
        val usersDTO = arrayListOf<RemoteUserDTO>()
        users.forEach { user ->
            val dto = userDTOFrom(user)
            usersDTO.add(dto)
        }
        return usersDTO
    }

    private fun userDTOFrom(user: QBUser?): RemoteUserDTO {
        val dto = RemoteUserDTO()
        dto.id = user?.id
        dto.name = user?.fullName
        dto.email = user?.email
        dto.login = user?.login
        dto.phone = user?.phone
        dto.website = user?.website
        dto.lastRequestAt = user?.lastRequestAt
        dto.externalId = user?.externalId
        dto.facebookId = user?.facebookId
        dto.blobId = user?.fileId
        dto.tags = user?.tags
        dto.customData = user?.customData
        return dto
    }
}