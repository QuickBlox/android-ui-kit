/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import android.content.Context
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.domain.repository.*

@ExcludeFromCoverage
open class DependencyImpl constructor(context: Context) : Dependency {
    private val dataSourceFactory: DataSourceFactory = DataSourceFactoryImpl(context)
    private val repositoryFactory: RepositoryFactory =
        RepositoryFactoryImpl(
            dataSourceFactory.createRemote(),
            dataSourceFactory.createLocal(),
            dataSourceFactory.createLocalFile(),
            dataSourceFactory.createAi()
        )

    private val connectionRepository = repositoryFactory.createConnection()
    private val dialogsRepository = repositoryFactory.createDialogs()
    private val filesRepository = repositoryFactory.createFiles()
    private val messageRepository = repositoryFactory.createMessages()
    private val usersRepository = repositoryFactory.createUsers()
    private val eventsRepository = repositoryFactory.createEvents()
    private val aiRepository = repositoryFactory.createAI()

    override fun getConnectionRepository(): ConnectionRepository {
        return connectionRepository
    }

    override fun getDialogsRepository(): DialogsRepository {
        return dialogsRepository
    }

    override fun getFilesRepository(): FilesRepository {
        return filesRepository
    }

    override fun getMessagesRepository(): MessagesRepository {
        return messageRepository
    }

    override fun getUsersRepository(): UsersRepository {
        return usersRepository
    }

    override fun getEventsRepository(): EventsRepository {
        return eventsRepository
    }

    override fun getAIRepository(): AIRepository {
        return aiRepository
    }
}