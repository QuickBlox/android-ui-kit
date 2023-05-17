/*
 * Created by Injoit on 08.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.stub.source

import com.quickblox.android_ui_kit.data.dto.local.file.LocalFileDTO
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource

open class LocalFileDataSourceStub : LocalFileDataSource {
    override fun getFile(dto: LocalFileDTO): LocalFileDTO {
        throw getRuntimeException()
    }

    override fun getFileByUri(dto: LocalFileDTO): LocalFileDTO {
        throw getRuntimeException()
    }

    override fun createFile(dto: LocalFileDTO) {
        throw getRuntimeException()
    }

    override fun createFile(extension: String): LocalFileDTO {
        throw getRuntimeException()
    }

    override fun deleteFile(dto: LocalFileDTO) {
        throw getRuntimeException()
    }

    override fun clearAll() {
        throw getRuntimeException()
    }

    private fun getRuntimeException(): RuntimeException {
        return RuntimeException("expected: override, actual: not override")
    }
}