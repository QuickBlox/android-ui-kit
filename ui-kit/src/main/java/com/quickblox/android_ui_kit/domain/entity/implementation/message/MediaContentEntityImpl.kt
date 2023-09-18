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

    @VisibleForTesting
    fun parseMediaContentTypeFrom(mimeType: String): MediaContentEntity.Types {
        val fileType = getFileTypeFrom(mimeType)
        when (fileType) {
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
                return MediaContentEntity.Types.FILE
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

    override fun isImage(): Boolean {
        return getType() == MediaContentEntity.Types.IMAGE
    }

    @VisibleForTesting
    fun getFileTypeFrom(mimeType: String): String {
        val fileSource = getSpitTypesFrom(mimeType)[0]
        return fileSource
    }

    @VisibleForTesting
    fun getFileExtensionFrom(mimeType: String): String {
        return try {
            getSpitTypesFrom(mimeType)[1]
        }catch (e: IndexOutOfBoundsException){
            mimeType
        }
    }

    @VisibleForTesting
    fun getSpitTypesFrom(mimeType: String): List<String> {
        return mimeType.split("/")
    }
}