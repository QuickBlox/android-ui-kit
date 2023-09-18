/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote.mapper

import android.text.TextUtils
import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.content.model.QBFile

object RemoteFileDTOMapper {
    fun toDTOFrom(qbFile: QBFile): RemoteFileDTO {
        val dto = RemoteFileDTO()
        dto.id = qbFile.id
        dto.uid = qbFile.uid
        dto.url = QBFile.getPrivateUrlForUID(dto.uid)
        dto.mimeType = getMimeTypeFrom(qbFile)
        dto.id = qbFile.id

        return dto
    }

    private fun getMimeTypeFrom(qbFile: QBFile): String {
        val isWrongContentType = TextUtils.isEmpty(qbFile.contentType) || qbFile.contentType.lowercase() == "unknown"
        if (isWrongContentType) {
            // TODO: need to add this logic in the future to get mime type by extension
            // val startExtensionIndex = qbFile.name.lastIndexOf(".") + 1
            // val extension = qbFile.name.substring(startExtensionIndex, qbFile.name.length)
            return "*/*"
        }
        return qbFile.contentType
    }
}