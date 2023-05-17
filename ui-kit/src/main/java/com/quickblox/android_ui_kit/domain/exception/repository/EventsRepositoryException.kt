/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception.repository

class EventsRepositoryException(val code: Codes, val description: String) : Exception(description) {
    enum class Codes { INCORRECT_DATA }
}