/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.stub

import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.domain.repository.*

open class DependencyStub : Dependency {
    override fun getConnectionRepository(): ConnectionRepository {
        throw RuntimeException()
    }

    override fun getDialogsRepository(): DialogsRepository {
        throw RuntimeException()
    }

    override fun getFilesRepository(): FilesRepository {
        throw RuntimeException()
    }

    override fun getMessagesRepository(): MessagesRepository {
        throw RuntimeException()
    }

    override fun getUsersRepository(): UsersRepository {
        throw RuntimeException()
    }

    override fun getEventsRepository(): EventsRepository {
        throw RuntimeException()
    }

    override fun getAIRepository(): AIRepository {
        throw RuntimeException()
    }
}