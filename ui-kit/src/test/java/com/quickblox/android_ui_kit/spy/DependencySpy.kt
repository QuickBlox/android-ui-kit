/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy

import com.quickblox.android_ui_kit.domain.repository.*
import com.quickblox.android_ui_kit.spy.repository.*
import com.quickblox.android_ui_kit.stub.DependencyStub

open class DependencySpy : DependencyStub() {
    private val connectionRepository = ConnectionRepositorySpy();
    private val dialogRepository = DialogsRepositorySpy();
    private val fileRepository = FileRepositorySpy();
    private val messagesRepository = MessagesRepositorySpy();
    private val usersRepository = UsersRepositorySpy();
    private val eventsRepository = EventsRepositorySpy();

    override fun getConnectionRepository(): ConnectionRepository {
        return connectionRepository
    }

    override fun getDialogsRepository(): DialogsRepository {
        return dialogRepository
    }

    override fun getFilesRepository(): FilesRepository {
        return fileRepository
    }

    override fun getMessagesRepository(): MessagesRepository {
        return messagesRepository
    }

    override fun getUsersRepository(): UsersRepository {
        return usersRepository
    }

    override fun getEventsRepository(): EventsRepository {
        return eventsRepository
    }
}