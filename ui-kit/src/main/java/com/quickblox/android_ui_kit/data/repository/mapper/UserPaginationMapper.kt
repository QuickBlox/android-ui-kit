/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl

object UserPaginationMapper {
    fun entityFrom(dto: RemoteUserPaginationDTO): PaginationEntity {
        val entity = PaginationEntityImpl()
        entity.setCurrentPage(dto.page)
        entity.setPerPage(dto.perPage)

        val hasNextPage = dto.page < dto.totalPages
        entity.setHasNextPage(hasNextPage)

        return entity
    }

    fun dtoFrom(entity: PaginationEntity): RemoteUserPaginationDTO {
        val dto = RemoteUserPaginationDTO()
        dto.perPage = entity.getPerPage()
        dto.page = entity.getCurrentPage()

        return dto
    }
}