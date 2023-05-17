/*
 * Created by Injoit on 5.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation.message

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity

class MediaContentEntityImpl(
    private val fileName: String,
    private val fileUrl: String,
    private val fileMimeType: String
) : MediaContentEntity {
    override fun getName(): String {
        return fileName
    }

    override fun getType(): MediaContentEntity.Types {
        return parseMediaContentTypeFrom(fileMimeType)
    }

    private fun parseMediaContentTypeFrom(mimeType: String): MediaContentEntity.Types {
        val fileType = getFileTypeFrom(mimeType)
        when (fileType) {
            MediaContentEntity.Types.FILE.value -> {
                return MediaContentEntity.Types.FILE
            }
            MediaContentEntity.Types.AUDIO.value -> {
                return MediaContentEntity.Types.AUDIO
            }
            MediaContentEntity.Types.VIDEO.value -> {
                return MediaContentEntity.Types.VIDEO
            }
            MediaContentEntity.Types.IMAGE.value -> {
                return MediaContentEntity.Types.IMAGE
            }
            else -> {
                throw IllegalArgumentException("Error parse file type from $fileType")
            }
        }
    }

    override fun getUrl(): String {
        return fileUrl
    }

    override fun getMimeType(): String {
        return fileMimeType
    }

    override fun isGif(): Boolean {
        return getFileExtensionFrom(getMimeType()).lowercase() == "gif"
    }

    @VisibleForTesting
    fun getFileTypeFrom(mimeType: String): String {
        val fileSource = getSpitTypesFrom(mimeType)[0]
        return fileSource
    }

    @VisibleForTesting
    fun getFileExtensionFrom(mimeType: String): String {
        val fileExtension = getSpitTypesFrom(mimeType)[1]
        return fileExtension
    }

    @VisibleForTesting
    fun getSpitTypesFrom(mimeType: String): List<String> {
        return mimeType.split("/")
    }
}