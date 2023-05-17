/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import com.quickblox.android_ui_kit.domain.repository.*

interface Dependency {
    fun getConnectionRepository(): ConnectionRepository
    fun getDialogsRepository(): DialogsRepository
    fun getFilesRepository(): FilesRepository
    fun getMessagesRepository(): MessagesRepository
    fun getUsersRepository(): UsersRepository
    fun getEventsRepository(): EventsRepository
}