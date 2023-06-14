/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.DialogsRepositoryException
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.domain.repository.DialogsRepository
import com.quickblox.android_ui_kit.domain.repository.MessagesRepository
import com.quickblox.android_ui_kit.spy.DependencySpy
import com.quickblox.android_ui_kit.spy.entity.message.IncomingChatMessageEntitySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import junit.framework.Assert.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeliverMessageUseCaseTest : BaseTest() {
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

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun buildMessageWithoutDialogId_execute_receivedError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val message = object : IncomingChatMessageEntitySpy() {
            override fun getDialogId(): String? {
                return null
            }
        }
        DeliverMessageUseCase(message).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun buildMessageWithoutSenderId_execute_receivedError() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())

        val message = object : IncomingChatMessageEntitySpy() {
            override fun getSenderId(): Int? {
                return null
            }
        }
        DeliverMessageUseCase(message).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun buildMessageAndDialogsRepositoryThrowsException_execute_receivedException() = runTest {
        val dialogsRepository = object : DialogsRepositorySpy() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                throw DialogsRepositoryException(DialogsRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })
        DeliverMessageUseCase(IncomingChatMessageEntitySpy()).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun buildMessageAndMessagesRepositoryThrowsException_execute_receivedException() = runTest {
        val messagesRepository = object : MessagesRepositorySpy() {
            override fun deliverMessage(entity: MessageEntity, dialog: DialogEntity) {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })
        DeliverMessageUseCase(IncomingChatMessageEntitySpy()).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessage_execute_noErrors() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        DeliverMessageUseCase(IncomingChatMessageEntitySpy()).execute()
    }
}