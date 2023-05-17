/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.repository

import com.quickblox.android_ui_kit.data.dto.remote.dialog.RemoteDialogDTO
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.repository.message.MessagesRepositoryImpl
import com.quickblox.android_ui_kit.data.source.remote.RemoteDataSourceExceptionFactoryImpl
import com.quickblox.android_ui_kit.domain.exception.repository.MessagesRepositoryException
import com.quickblox.android_ui_kit.spy.entity.DialogEntitySpy
import com.quickblox.android_ui_kit.spy.entity.message.OutgoingChatMessageEntitySpy
import com.quickblox.android_ui_kit.stub.source.RemoteDataSourceStub
import com.quickblox.core.exception.QBResponseException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MessageRepositoryTest {
    private val remoteExceptionFactory = RemoteDataSourceExceptionFactoryImpl()

    @Test
    fun sendMessageToRemote_NoException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun sendChatMessage(messageDTO: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
                // sent message
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        messagesRepository.sendChatMessageToRemote(OutgoingChatMessageEntitySpy(), DialogEntitySpy())
    }

    @Test
    fun sendMessageToRemote_NotConnectedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun sendChatMessage(dto: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
                throw remoteExceptionFactory.makeConnectionFailed("Not connected")
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        try {
            messagesRepository.sendChatMessageToRemote(OutgoingChatMessageEntitySpy(), DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: MessagesRepositoryException) {
            assertEquals(MessagesRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    fun sendMessageToRemote_IllegalStateException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun sendChatMessage(dto: RemoteMessageDTO, dialogDTO: RemoteDialogDTO) {
                throw remoteExceptionFactory.makeRestrictedAccess("Restricted Access")
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        try {
            messagesRepository.sendChatMessageToRemote(OutgoingChatMessageEntitySpy(), DialogEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: MessagesRepositoryException) {
            assertEquals(MessagesRepositoryException.Codes.RESTRICTED_ACCESS, exception.code)
        }
    }

    @Test
    fun updateMessageInRemote_NoException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateMessage(dto: RemoteMessageDTO) {
                // updating message
            }
        }

        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        messagesRepository.updateMessageInRemote(OutgoingChatMessageEntitySpy())
    }

    @Test
    fun updateMessageInRemote_ConnectionFailedException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun updateMessage(dto: RemoteMessageDTO) {
                val serviceUnavailableCode = 503
                val errors = arrayListOf<String>()
                errors.add("Service unavailable")

                val responseException = QBResponseException(serviceUnavailableCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        try {
            messagesRepository.updateMessageInRemote(OutgoingChatMessageEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: MessagesRepositoryException) {
            assertEquals(MessagesRepositoryException.Codes.CONNECTION_FAILED, exception.code)
        }
    }

    @Test
    fun deleteMessageInRemote_NoException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun deleteMessage(dto: RemoteMessageDTO) {
                // empty
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        messagesRepository.deleteMessageInRemote(OutgoingChatMessageEntitySpy())
    }

    @Test
    fun deleteMessageInRemote_NotFoundException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun deleteMessage(dto: RemoteMessageDTO) {
                val notFoundCode = 404
                val errors = arrayListOf<String>()
                errors.add("Not found")

                val responseException = QBResponseException(notFoundCode, errors)
                throw remoteExceptionFactory.makeBy(
                    responseException.httpStatusCode,
                    responseException.message.toString()
                )
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        try {
            messagesRepository.deleteMessageInRemote(OutgoingChatMessageEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: MessagesRepositoryException) {
            assertEquals(MessagesRepositoryException.Codes.NOT_FOUND_ITEM, exception.code)
        }
    }

    @Test
    fun deleteMessageInRemote_IncorrectDataException() {
        val remoteDataSource = object : RemoteDataSourceStub() {
            override fun deleteMessage(dto: RemoteMessageDTO) {
                throw remoteExceptionFactory.makeIncorrectData("Incorrect data")
            }
        }
        val messagesRepository = MessagesRepositoryImpl(remoteDataSource)
        try {
            messagesRepository.deleteMessageInRemote(OutgoingChatMessageEntitySpy())
            fail("expect: Exception, actual: No Exception")
        } catch (exception: MessagesRepositoryException) {
            assertEquals(MessagesRepositoryException.Codes.INCORRECT_DATA, exception.code)
        }
    }
}