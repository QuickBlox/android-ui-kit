/*
 * Created by Injoit on 24.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.exception

import com.quickblox.android_ui_kit.ExcludeFromCoverage

@ExcludeFromCoverage
class DomainException(description: String) : Exception(description) {
    @ExcludeFromCoverage
    enum class Codes { UNEXPECTED }
}