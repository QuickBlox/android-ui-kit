/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.connection

import com.quickblox.android_ui_kit.domain.exception.repository.ConnectionRepositoryException

class ConnectionRepositoryExceptionFactoryImpl : ConnectionRepositoryExceptionFactory {
    override fun makeUnexpected(description: String): ConnectionRepositoryException {
        return ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNEXPECTED, description)
    }

    override fun makeUnauthorised(description: String): ConnectionRepositoryException {
        return ConnectionRepositoryException(ConnectionRepositoryException.Codes.UNAUTHORIZED, description)
    }

    override fun makeAlreadyLogged(description: String): ConnectionRepositoryException {
        return ConnectionRepositoryException(ConnectionRepositoryException.Codes.ALREADY_LOGGED, description)
    }
}