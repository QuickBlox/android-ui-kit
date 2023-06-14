/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception.repository

import com.quickblox.android_ui_kit.ExcludeFromCoverage

@ExcludeFromCoverage
class FilesRepositoryException(val code: Codes, val description: String) : Exception(description) {
    @ExcludeFromCoverage
    enum class Codes {
        UNEXPECTED,
        NOT_FOUND_ITEM,
        ALREADY_EXIST,
        UNAUTHORISED,
        INCORRECT_DATA,
        RESTRICTED_ACCESS,
        CONNECTION_FAILED,
        WRITE_AND_READ
    }
}