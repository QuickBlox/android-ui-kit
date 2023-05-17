/*
 * Created by Injoit on 24.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository.event

import com.quickblox.android_ui_kit.domain.exception.repository.EventsRepositoryException

class EventsRepositoryExceptionFactoryImpl : EventsRepositoryExceptionFactory {
    override fun makeIncorrectData(description: String): EventsRepositoryException {
        return EventsRepositoryException(EventsRepositoryException.Codes.INCORRECT_DATA, description)
    }
}
