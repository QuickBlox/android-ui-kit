/*
 * Created by Injoit on 08.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.source.exception

class LocalFileDataSourceException(val code: Codes, val description: String) : Exception(description) {
    enum class Codes { UNEXPECTED, NOT_FOUND_ITEM, ALREADY_EXIST, WRITE_AND_READ, INCORRECT_DATA, RESTRICTED_ACCESS }
}