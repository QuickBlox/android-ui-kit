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
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.entity.message.IncomingChatMessageEntitySpy
import com.quickblox.android_ui_kit.spy.repository.DialogsRepositorySpy
import com.quickblox.android_ui_kit.spy.repository.MessagesRepositorySpy
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReadMessageUseCaseTest : BaseTest() {
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
    fun makeCorrectSenderId_isWrongSender_receivedFalse() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val isWrongSenderId = ReadMessageUseCase(IncomingChatMessageEntitySpy()).isWrongSenderId(888)
        assertFalse(isWrongSenderId)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun makeInCorrectSenderId_isWrongSender_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val isWrongSenderId = ReadMessageUseCase(IncomingChatMessageEntitySpy()).isWrongSenderId(-777)
        assertTrue(isWrongSenderId)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun makeNullSenderId_isWrongSender_receivedTrue() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val isWrongSenderId = ReadMessageUseCase(IncomingChatMessageEntitySpy()).isWrongSenderId(null)
        assertTrue(isWrongSenderId)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun makeNullUnreadMessageCount_modifyUnreadMessageCount_receivedNULL() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val unreadMessageCount = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCount(null)
        assertNull(unreadMessageCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun makeNegativeUnreadMessageCount_modifyUnreadMessageCount_receivedZero() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val unreadMessageCount = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCount(-17)
        assertEquals(0, unreadMessageCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun make17UnreadMessageCount_modifyUnreadMessageCount_received16() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val unreadMessageCount = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCount(17)
        assertEquals(16, unreadMessageCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun makeZeroUnreadMessageCount_modifyUnreadMessageCount_receivedZero() {
        QuickBloxUiKit.setDependency(DependencySpy())

        val unreadMessageCount = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCount(0)
        assertEquals(0, unreadMessageCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialogWith7UnreadMessageCount_updateUnreadMessageCountInDialog_receivedDialogWith6UnreadMessageCount() =
        runTest {
            QuickBloxUiKit.setDependency(DependencySpy())

            val dialog = DialogEntitySpy()
            dialog.setUnreadMessagesCount(7)

            val updatedDialog = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCountIn(dialog)
            assertEquals(6, updatedDialog.getUnreadMessagesCount())
        }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialogWithZeroUnreadMessageCount_updateUnreadMessageCountInDialog_receivedDialogWithZeroUnreadMessageCount() =
        runTest {
            QuickBloxUiKit.setDependency(DependencySpy())

            val dialog = DialogEntitySpy()
            dialog.setUnreadMessagesCount(0)

            val updatedDialog = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCountIn(dialog)
            assertEquals(0, updatedDialog.getUnreadMessagesCount())
        }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialogWithNegativeUnreadMessageCount_updateUnreadMessageCountInDialog_receivedDialogWithZeroUnreadMessageCount() =
        runTest {
            QuickBloxUiKit.setDependency(DependencySpy())

            val dialog = DialogEntitySpy()
            dialog.setUnreadMessagesCount(-7)

            val updatedDialog = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCountIn(dialog)
            assertEquals(0, updatedDialog.getUnreadMessagesCount())
        }

    @Test
    @ExperimentalCoroutinesApi
    fun buildDialogWithNullUnreadMessageCount_updateUnreadMessageCountInDialog_receivedDialogWithNullUnreadMessageCount() =
        runTest {
            QuickBloxUiKit.setDependency(DependencySpy())

            val dialog = DialogEntitySpy()
            dialog.setUnreadMessagesCount(null)

            val updatedDialog = ReadMessageUseCase(IncomingChatMessageEntitySpy()).updateUnreadMessageCountIn(dialog)
            assertNull(updatedDialog.getUnreadMessagesCount())
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
        ReadMessageUseCase(message).execute()
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
        ReadMessageUseCase(message).execute()
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
        ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test(expected = DomainException::class)
    @ExperimentalCoroutinesApi
    fun buildMessageAndMessagesRepositoryThrowsException_execute_receivedException() = runTest {
        val messagesRepository = object : MessagesRepositorySpy() {
            override fun readMessage(entity: MessageEntity, dialog: DialogEntity) {
                throw MessagesRepositoryException(MessagesRepositoryException.Codes.INCORRECT_DATA, "")
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getMessagesRepository(): MessagesRepository {
                return messagesRepository
            }
        })
        ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()
        fail("expected: Exception, actual: NoException")
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessage_execute_noErrors() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessageAndHaveDialogWith17UnreadMessageCount_execute_receivedDialogWith16UnreadMessageCount() = runTest {
        var receivedUnreadMessageCount: Int? = null

        val dialogsRepository = object : DialogsRepositorySpy() {
            override fun getDialogFromLocal(dialogId: String): DialogEntity {
                val dialog = DialogEntitySpy()
                dialog.setUnreadMessagesCount(17)
                return dialog
            }

            override suspend fun updateDialogInLocal(entity: DialogEntity) {
                receivedUnreadMessageCount = entity.getUnreadMessagesCount()
            }
        }

        QuickBloxUiKit.setDependency(object : DependencySpy() {
            override fun getDialogsRepository(): DialogsRepository {
                return dialogsRepository
            }
        })

        ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()

        assertEquals(16, receivedUnreadMessageCount)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessageAndHaveDialogWithZeroUnreadMessageCount_execute_receivedDialogWithZeroUnreadMessageCount() =
        runTest {
            var receivedUnreadMessageCount: Int? = null

            val dialogsRepository = object : DialogsRepositorySpy() {
                override fun getDialogFromLocal(dialogId: String): DialogEntity {
                    val dialog = DialogEntitySpy()
                    dialog.setUnreadMessagesCount(0)
                    return dialog
                }

                override suspend fun updateDialogInLocal(entity: DialogEntity) {
                    receivedUnreadMessageCount = entity.getUnreadMessagesCount()
                }
            }

            QuickBloxUiKit.setDependency(object : DependencySpy() {
                override fun getDialogsRepository(): DialogsRepository {
                    return dialogsRepository
                }
            })

            ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()

            assertEquals(0, receivedUnreadMessageCount)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessageAndHaveDialogWithNegativeUnreadMessageCount_execute_receivedDialogWithZeroUnreadMessageCount() =
        runTest {
            var receivedUnreadMessageCount: Int? = null

            val dialogsRepository = object : DialogsRepositorySpy() {
                override fun getDialogFromLocal(dialogId: String): DialogEntity {
                    val dialog = DialogEntitySpy()
                    dialog.setUnreadMessagesCount(-77)
                    return dialog
                }

                override suspend fun updateDialogInLocal(entity: DialogEntity) {
                    receivedUnreadMessageCount = entity.getUnreadMessagesCount()
                }
            }

            QuickBloxUiKit.setDependency(object : DependencySpy() {
                override fun getDialogsRepository(): DialogsRepository {
                    return dialogsRepository
                }
            })

            ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()

            assertEquals(0, receivedUnreadMessageCount)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun buildMessageAndHaveDialogWithNullUnreadMessageCount_execute_receivedDialogWithNullUnreadMessageCount() =
        runTest {
            var receivedUnreadMessageCount: Int? = null

            val dialogsRepository = object : DialogsRepositorySpy() {
                override fun getDialogFromLocal(dialogId: String): DialogEntity {
                    val dialog = DialogEntitySpy()
                    dialog.setUnreadMessagesCount(null)
                    return dialog
                }

                override suspend fun updateDialogInLocal(entity: DialogEntity) {
                    receivedUnreadMessageCount = entity.getUnreadMessagesCount()
                }
            }

            QuickBloxUiKit.setDependency(object : DependencySpy() {
                override fun getDialogsRepository(): DialogsRepository {
                    return dialogsRepository
                }
            })

            ReadMessageUseCase(IncomingChatMessageEntitySpy()).execute()

            assertNull(receivedUnreadMessageCount)
        }
}