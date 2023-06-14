/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.TypingEntityImpl
import junit.framework.Assert.*
import org.junit.Test

class TypingEntityTest {
    @Test
    fun buildTypingEntity_setTextAndGetText_textsEquals() {
        val tempText = System.currentTimeMillis().toString()

        val entity = TypingEntityImpl()
        entity.setText(tempText)

        assertEquals(tempText, entity.getText())
    }

    @Test
    fun buildTypingEntity_setText_isStartedTrue() {
        val entity = TypingEntityImpl()
        entity.setText("test text")

        assertTrue(entity.isStarted())
    }

    @Test
    fun buildTypingEntity_setText_isStoppedFalse() {
        val entity = TypingEntityImpl()
        entity.setText("test text")

        assertFalse(entity.isStopped())
    }

    @Test
    fun buildTypingEntity_setEmptyText_isStoppedTrue() {
        val entity = TypingEntityImpl()
        entity.setText("")

        assertTrue(entity.isStopped())
    }

    @Test
    fun buildTypingEntity_setEmptyText_isStartedFalse() {
        val entity = TypingEntityImpl()
        entity.setText("")

        assertFalse(entity.isStarted())
    }
}