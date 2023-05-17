/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.exception

class RemoteDataSourceException(val code: Codes, val description: String) : Exception(description) {
    enum class Codes {
        UNEXPECTED,
        NOT_FOUND_ITEM,
        UNAUTHORISED,
        INCORRECT_DATA,
        RESTRICTED_ACCESS,
        CONNECTION_FAILED,
        ALREADY_LOGGED
    }
}