/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import com.quickblox.android_ui_kit.domain.repository.*

interface RepositoryFactory {
    fun createConnection(): ConnectionRepository
    fun createDialogs(): DialogsRepository
    fun createFiles(): FilesRepository
    fun createMessages(): MessagesRepository
    fun createUsers(): UsersRepository
    fun createEvents(): EventsRepository
    fun createAI(): AIRepository
}