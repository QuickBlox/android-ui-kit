/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.repository.mapper

import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessagePaginationDTO
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl

object MessagePaginationMapper {
    fun dtoFrom(entity: PaginationEntity): RemoteMessagePaginationDTO {
        val dto = RemoteMessagePaginationDTO()
        dto.page = entity.getCurrentPage()
        dto.perPage = entity.getPerPage()

        return dto
    }

    fun entityFrom(dto: RemoteMessagePaginationDTO): PaginationEntity {
        val entity = PaginationEntityImpl()
        entity.setCurrentPage(dto.page)
        entity.setPerPage(dto.perPage)

        val hasNextPage = dto.resultCount == dto.perPage
        entity.setHasNextPage(hasNextPage)

        return entity
    }
}