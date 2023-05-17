/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.connection

import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException

interface ConnectionRepositoryExceptionFactory {
    fun makeUnexpected(description: String): ConnectionRepositoryException
    fun makeUnauthorised(description: String): ConnectionRepositoryException
    fun makeAlreadyLogged(description: String): ConnectionRepositoryException
}
