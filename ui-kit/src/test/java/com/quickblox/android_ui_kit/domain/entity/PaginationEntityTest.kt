/*
 * Created by Injoit on 28.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import junit.framework.Assert.*
import org.junit.Test
import kotlin.random.Random

class PaginationEntityTest {
    @Test
    fun buildPaginationEntity_setFields_fieldsAreEquals() {
        val currentPage = Random.nextInt(1000, 2000)
        val perPage = Random.nextInt(2000, 3000)
        val hasNextPage = Random.nextBoolean()

        val entity = PaginationEntityImpl()
        entity.setCurrentPage(currentPage)
        entity.setPerPage(perPage)
        entity.setHasNextPage(hasNextPage)

        assertEquals(currentPage, entity.getCurrentPage())
        assertEquals(perPage, entity.getPerPage())
        assertEquals(hasNextPage, entity.hasNextPage())
    }

    @Test
    fun buildTypingEntity_setHasNextPageTrue_nextPageReturnTrueAndCurrentPageIs2() {
        val entity = PaginationEntityImpl()
        entity.setHasNextPage(true)

        assertTrue(entity.nextPage())
        assertEquals(2, entity.getCurrentPage())
    }

    @Test
    fun buildTypingEntity_setHasNextPageFalse_nextPageReturnFalseAndCurrentPageIs1() {
        val entity = PaginationEntityImpl()
        entity.setHasNextPage(false)

        assertFalse(entity.nextPage())
        assertEquals(1, entity.getCurrentPage())
    }
}