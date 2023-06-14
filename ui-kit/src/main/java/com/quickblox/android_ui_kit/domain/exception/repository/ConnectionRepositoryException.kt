/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception.repository

import com.quickblox.android_ui_kit.ExcludeFromCoverage

@ExcludeFromCoverage
class ConnectionRepositoryException(val code: Codes, val description: String) : Exception(description) {
    @ExcludeFromCoverage
    enum class Codes {
        UNEXPECTED,
        UNAUTHORIZED,
        ALREADY_LOGGED
    }
}