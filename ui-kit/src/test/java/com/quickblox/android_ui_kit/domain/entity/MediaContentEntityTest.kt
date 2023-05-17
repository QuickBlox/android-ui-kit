/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import org.junit.Assert.*
import org.junit.Test

class MediaContentEntityTest {
    @Test
    fun createdMediaContentWithGif_getFileType_receivedImage() {
        val entity = MediaContentEntityImpl("test","https://test.com/test.gif", "image/gif")
        val fileType = entity.getFileTypeFrom("image/gif")
        assertEquals("image", fileType)
    }

    @Test
    fun createdMediaContentWithGif_getFileExtension_receivedGif() {
        val entity = MediaContentEntityImpl("test","https://test.com/test.gif", "image/gif")
        val fileExtensionFrom = entity.getFileExtensionFrom("image/gif")
        assertEquals("gif", fileExtensionFrom)
    }

    @Test
    fun createdMediaContentWithGif_getSpitTypes_receivedImageAndGif() {
        val entity = MediaContentEntityImpl("test","https://test.com/test.gif", "image/gif")
        val splitTypes = entity.getSpitTypesFrom("image/gif")
        assertEquals("image", splitTypes[0])
        assertEquals("gif", splitTypes[1])
    }

    @Test
    fun createdMediaContentWithGif_checkIsGif_receivedTrue() {
        val entity = MediaContentEntityImpl("test","https://test.com/test.gif", "image/gif")
        val isGif = entity.isGif()
        assertTrue(isGif)
    }

    @Test
    fun createdMediaContentWithJpg_checkIsGif_receivedFalse() {
        val entity = MediaContentEntityImpl("test","https://test.com/test.jpg", "image/jpeg")
        val isGif = entity.isGif()
        assertFalse(isGif)
    }
}