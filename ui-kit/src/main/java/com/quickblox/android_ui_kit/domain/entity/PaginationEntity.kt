/*
 * Created by Injoit on 20.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.entity

interface PaginationEntity {
    fun setCurrentPage(page: Int)
    fun getCurrentPage(): Int
    fun setPerPage(count: Int)
    fun getPerPage(): Int
    fun hasNextPage(): Boolean
    fun setHasNextPage(hasNextPage: Boolean)
    fun nextPage(): Boolean
}