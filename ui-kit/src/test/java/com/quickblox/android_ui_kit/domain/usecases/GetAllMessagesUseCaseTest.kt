/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.source.exception.RemoteDataSourceException
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class GetAllMessagesUseCaseTest : BaseTest() {
    @Before
    @ExperimentalCoroutinesApi
    fun init() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    @ExperimentalCoroutinesApi
    fun release() {
        Dispatchers.resetMain()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun availableMessages_execute_receivedMessages() = runTest {
        val loadedMessagesCount = 15

        val messagesRepository = MessagesRepositorySpy(remoteMessagesCount = loadedMessagesCount)

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        val completionLatch = CountDownLatch(1)
        val loadedMessages = mutableListOf<MessageEntity>()
        withContext(UnconfinedTestDispatcher()) {
            GetAllMessagesUseCase(DialogEntitySpy(), PaginationEntityImpl()).execute()
                .onCompletion {
                    completionLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    result.getOrThrow().first?.let { message ->
                        loadedMessages.add(message)
                    }
                }
        }

        assertEquals(0, completionLatch.count)
        assertEquals(loadedMessagesCount, loadedMessages.size)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun availableMessages_executeWithEmptyDialogId_receivedException() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val errorLatch = CountDownLatch(1)
        withContext(UnconfinedTestDispatcher()) {
            val dialogWithEmptyId = DialogEntitySpy()
            dialogWithEmptyId.setDialogId("")
            GetAllMessagesUseCase(dialogWithEmptyId, PaginationEntityImpl()).execute()
                .catch { error ->
                    errorLatch.countDown()
                }.collect { result ->
                    Assert.fail("expected: Exception, actual: NoException")
                }
        }

        assertEquals(0, errorLatch.count)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun getMessagesFromRemoteReturnErrorResult_execute_receivedZeroMessages() = runTest {
        val loadedMessagesCount = 15

        val messagesRepository = object : MessagesRepositorySpy() {
            override fun getMessagesFromRemote(
                dialogId: String,
                paginationEntity: PaginationEntity
            ): Flow<Result<Pair<MessageEntity, PaginationEntity>>> {
                return channelFlow {
                    for (index in 1..loadedMessagesCount) {
                        val exception = RemoteDataSourceException(RemoteDataSourceException.Codes.INCORRECT_DATA, "")
                        send(Result.failure(exception))
                    }
                }
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })

        val completionLatch = CountDownLatch(1)
        var loadedFailureResultCount = 0
        withContext(UnconfinedTestDispatcher()) {
            GetAllMessagesUseCase(DialogEntitySpy(), PaginationEntityImpl()).execute()
                .onCompletion {
                    completionLatch.countDown()
                }.catch { error ->
                    Assert.fail("expected: NotException, actual: Exception, details: $error")
                }.collect { result ->
                    if (result.isSuccess) {
                        Assert.fail("expected: Failure, actual: Success")
                    }
                    if (result.isFailure) {
                        ++loadedFailureResultCount
                    }
                }
        }

        assertEquals(0, completionLatch.count)
        assertEquals(loadedMessagesCount, loadedFailureResultCount)
    }
}