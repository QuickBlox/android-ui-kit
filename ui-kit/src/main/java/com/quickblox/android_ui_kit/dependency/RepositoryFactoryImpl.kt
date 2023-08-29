/*
 * Created by Injoit on 20.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.dependency

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.data.repository.ai.AIRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.connection.ConnectionRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.dialog.DialogsRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.event.EventsRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.file.FilesRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.message.MessagesRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.user.UsersRepositoryImpl
import com.quickblox.android_ui_kit.data.source.ai.AIDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalDataSource
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.domain.repository.*

@ExcludeFromCoverage
class RepositoryFactoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val localFileDataSource: LocalFileDataSource,
    private val aiDataSource: AIDataSource,
) : RepositoryFactory {
    override fun createConnection(): ConnectionRepository {
        return ConnectionRepositoryImpl(remoteDataSource)
    }

    override fun createDialogs(): DialogsRepository {
        return DialogsRepositoryImpl(remoteDataSource, localDataSource)
    }

    override fun createFiles(): FilesRepository {
        return FilesRepositoryImpl(remoteDataSource, localFileDataSource)
    }

    override fun createMessages(): MessagesRepository {
        return MessagesRepositoryImpl(remoteDataSource)
    }

    override fun createUsers(): UsersRepository {
        return UsersRepositoryImpl(remoteDataSource, localDataSource)
    }

    override fun createEvents(): EventsRepository {
        return EventsRepositoryImpl(remoteDataSource, localDataSource)
    }

    override fun createAI(): AIRepository {
        return AIRepositoryImpl(aiDataSource)
    }
}