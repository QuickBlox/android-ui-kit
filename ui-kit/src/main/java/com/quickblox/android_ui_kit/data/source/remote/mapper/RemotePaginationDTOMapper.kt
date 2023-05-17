/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.mapper

import android.os.Bundle
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessagePaginationDTO
import com.quickblox.android_ui_kit.data.dto.remote.user.RemoteUserPaginationDTO
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.core.request.QBRequestGetBuilder

object RemotePaginationDTOMapper {
    fun remoteMessagePaginationDtoFrom(resultCount: Int, currentPage: Int, perPage: Int): RemoteMessagePaginationDTO {
        val dto = RemoteMessagePaginationDTO()
        dto.page = currentPage
        dto.perPage = perPage
        dto.resultCount = resultCount

        return dto
    }

    fun remoteUserPaginationDtoFrom(bundle: Bundle): RemoteUserPaginationDTO {
        val dto = RemoteUserPaginationDTO()
        dto.page = bundle.getInt("current_page")
        dto.perPage = bundle.getInt("per_page")
        dto.totalPages = bundle.getInt("total_pages")

        return dto
    }

    fun getRequestBuilderFrom(dto: RemoteMessagePaginationDTO): QBRequestGetBuilder {
        val requestBuilder = QBRequestGetBuilder()

        if (dto.page > 1) {
            val page = dto.page - 1
            val skip = dto.perPage * page

            requestBuilder.skip = skip
        } else {
            requestBuilder.skip = 0
        }

        requestBuilder.limit = dto.perPage
        requestBuilder.sortDesc("date_sent")

        return requestBuilder
    }

    fun pagedRequestBuilderFrom(dto: RemoteUserPaginationDTO): QBPagedRequestBuilder {
        val requestBuilder = QBPagedRequestBuilder()

        requestBuilder.page = dto.page
        requestBuilder.perPage = dto.perPage
        return requestBuilder
    }
}