/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception.repository

class DialogsRepositoryException(val code: Codes, val description: String) : Exception(description) {
    enum class Codes {
        UNEXPECTED,
        NOT_FOUND_ITEM,
        ALREADY_EXIST,
        UNAUTHORISED,
        INCORRECT_DATA,
        RESTRICTED_ACCESS,
        CONNECTION_FAILED,
    }
}