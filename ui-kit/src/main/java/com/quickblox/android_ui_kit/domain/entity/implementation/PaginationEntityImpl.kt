/*
 * Created by Injoit on 24.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.domain.entity.PaginationEntity

class PaginationEntityImpl : PaginationEntity {
    private var currentPage: Int = 1
    private var perPage: Int = 100
    private var hasNextPage: Boolean = false

    override fun setCurrentPage(page: Int) {
        currentPage = page
    }

    override fun getCurrentPage(): Int {
        return currentPage
    }

    override fun setPerPage(count: Int) {
        perPage = count
    }

    override fun getPerPage(): Int {
        return perPage
    }

    override fun hasNextPage(): Boolean {
        return hasNextPage
    }

    override fun setHasNextPage(hasNextPage: Boolean) {
        this.hasNextPage = hasNextPage
    }

    override fun nextPage(): Boolean {
        if (hasNextPage()) {
            currentPage += 1
            return true
        }
        return false
    }
}