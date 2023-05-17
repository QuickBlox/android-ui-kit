/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.remote.mapper

import com.quickblox.android_ui_kit.data.dto.remote.file.RemoteFileDTO
import com.quickblox.content.model.QBFile

object RemoteFileDTOMapper {
    fun toDTOFrom(qbFile: QBFile): RemoteFileDTO {
        val dto = RemoteFileDTO()
        dto.id = qbFile.id
        dto.uid = qbFile.uid
        dto.url = qbFile.publicUrl
        dto.mimeType = qbFile.contentType
        dto.id = qbFile.id

        return dto
    }
}