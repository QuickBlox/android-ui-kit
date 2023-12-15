/*
 * Created by Injoit on 4.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.repository.ai.AIRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.connection.ConnectionRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.dialog.DialogsRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.event.EventsRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.file.FilesRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.message.MessagesRepositoryImpl
import com.quickblox.android_ui_kit.data.repository.user.UsersRepositoryImpl
import com.quickblox.android_ui_kit.data.source.ai.AIDataSourceImpl
import com.quickblox.android_ui_kit.data.source.local.LocalDataSourceImpl
import com.quickblox.android_ui_kit.data.source.local.LocalFileDataSourceImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSource
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceImpl
import com.quickblox.android_ui_kit.dependency.Dependency
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.repository.*
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.android_ui_kit.domain.usecases.DialogsEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogsUseCase
import com.quickblox.android_ui_kit.domain.usecases.SyncDialogsUseCase
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.request.QBRequestGetBuilder
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onCompletion
import org.jivesoftware.smack.SmackException.NotConnectedException
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DialogsEventUseCaseIntegrationTest : BaseTest() {
    @Before
    fun init() {
        initQuickblox()
        loginToRest()
    }

    @After
    fun release() {
        logoutFromChat()
        logoutFromRest()
    }

    @Test
    @Ignore("need to fix coroutine/flow behaviour")
    @ExperimentalCoroutinesApi
    fun syncDialogs_sendMessage_dialogReceived() = runBlocking {
        initDependency()

        val connectionJob = CoroutineScope(Dispatchers.IO).launch {
            ConnectionUseCase().execute()
        }

        val syncDialogsJob = CoroutineScope(Dispatchers.IO).launch {
            SyncDialogsUseCase().execute()
        }

        val sentMessageText = "test_message: ${System.currentTimeMillis()}"

        val sendMessageJob = sendMessage(sentMessageText)

        //todo: need to fix the test. Problem in LocalDataSourceImpl in method "saveDialog()"
        //todo: the DialogsEventUseCase received dialog entity from saveDialog() method instead of updateDialog() method
        //CountDownLatch(1).await(2, TimeUnit.SECONDS)

        var receivedMessageText = ""
        val receivedDialogLatch = CountDownLatch(1)
        val dialogsEventsJob = CoroutineScope(Dispatchers.IO).launch {
            DialogsEventUseCase().execute().collect { dialogEntity ->
                dialogEntity?.let {
                    receivedMessageText = dialogEntity.getLastMessage()?.getContent()!!
                    receivedDialogLatch.countDown()
                }
            }
        }

        receivedDialogLatch.await(10, TimeUnit.SECONDS)

        syncDialogsJob.cancel()
        sendMessageJob.cancel()
        dialogsEventsJob.cancel()
        connectionJob.cancel()

        assertEquals(0, receivedDialogLatch.count)
        //assertEquals(sentMessageText, receivedMessageText)
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

        val localDataSource = LocalDataSourceImpl()
        val remoteDataSource = buildRemoteDataSource(1)
        val aiDataSource = AIDataSourceImpl()

        val connectionRepository = ConnectionRepositoryImpl(remoteDataSource)
        val dialogsRepository = DialogsRepositoryImpl(remoteDataSource, localDataSource)
        val fileRepository = FilesRepositoryImpl(remoteDataSource, LocalFileDataSourceImpl(context))
        val messageRepository = MessagesRepositoryImpl(remoteDataSource)
        val usersRepository = UsersRepositoryImpl(remoteDataSource, localDataSource)
        val eventsRepository = EventsRepositoryImpl(remoteDataSource, localDataSource)
        val aiRepository = AIRepositoryImpl(aiDataSource)

        val dependency = object : Dependency {
            override fun getConnectionRepository(): ConnectionRepository {
                return connectionRepository
            }

            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }

            override fun getFilesRepository(): FilesRepository {
                return fileRepository
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

        QuickBloxUiKit.setDependency(dependency)
    }

    private fun sendMessage(sentMessageText: String): Job {
        val sendMessageLatch = CountDownLatch(1)

        var loadedDialogEntity: DialogEntity? = null
        val job = CoroutineScope(Dispatchers.IO).launch {
            GetDialogsUseCase().execute().onCompletion {
                val qbDialog = buildQBChatDialogFrom(loadedDialogEntity!!)
                sendMessageTo(qbDialog, sentMessageText)
                sendMessageLatch.countDown()
            }.collect { result ->
                if (result.isSuccess) {
                    loadedDialogEntity = result.getOrThrow()
                }
            }
        }
        sendMessageLatch.await(10, TimeUnit.SECONDS)

        assertEquals(0, sendMessageLatch.count)

        return job
    }

    private fun buildQBChatDialogFrom(dialogEntity: DialogEntity): QBChatDialog {
        val qbDialog = QBChatDialog()

        val dialogType = dialogEntity.getType()
        if (dialogType == DialogEntity.Types.PRIVATE) {
            qbDialog.type = QBDialogType.PRIVATE
        } else if (dialogType == DialogEntity.Types.GROUP) {
            qbDialog.type = QBDialogType.GROUP
        }

        qbDialog.dialogId = dialogEntity.getDialogId()

        qbDialog.setOccupantsIds(dialogEntity.getParticipantIds()?.toList())

        return qbDialog
    }

    private fun sendMessageTo(qbDialog: QBChatDialog, message: String) {
        try {
            qbDialog.initForChat(QBChatService.getInstance())
            qbDialog.sendMessage(message)
        } catch (e: IllegalArgumentException) {
            e.toString()
        } catch (e: NotConnectedException) {
            e.toString()
        }
    }

    private fun buildRemoteDataSource(loadedDialogsCount: Int): RemoteDataSource {
        return object : RemoteDataSourceImpl() {
            override fun loadAllQBDialogs(requestBuilder: QBRequestGetBuilder): List<QBChatDialog> {
                val modifiedRequestBuilder = QBRequestGetBuilder()
                modifiedRequestBuilder.limit = loadedDialogsCount

                return super.loadAllQBDialogs(modifiedRequestBuilder)
            }
        }
    }
}