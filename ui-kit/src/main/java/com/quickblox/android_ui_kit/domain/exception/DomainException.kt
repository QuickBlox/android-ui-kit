/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception

class DomainException(description: String) : Exception(description) {
    enum class Codes { UNEXPECTED }
}