/*
 * Created by Injoit on 25.1.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quickblox.android_ui_kit.BaseTest
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.dependency.DependencyImpl
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.usecases.CreateMessageUseCase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class CreateMessageIntegrationTest : BaseTest() {
    @Before
    fun init() {
        initDependency()
        initQuickblox()
        loginToRest()
    }

    private fun initDependency() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        QuickBloxUiKit.setDependency(DependencyImpl(context))
    }

    @After
    fun release() {
        logoutFromRest()
    }

    @Test
    fun haveArgumentForTextMessage_execute_receiveTextMessage() = runBlocking {
        val contentType = ChatMessageEntity.ContentTypes.TEXT
        val dialogId = System.currentTimeMillis().toString()
        val content = System.currentTimeMillis().toString()

        val receivedMessageLatch = CountDownLatch(1)
        CreateMessageUseCase(contentType, dialogId, content).execute().collect { createdMessage ->
            assertTrue(createdMessage?.geMessageId()!!.isNotEmpty())
            assertEquals(contentType, createdMessage.getContentType())
            assertEquals(dialogId, createdMessage.getDialogId())
            assertEquals(content, createdMessage.getContent())

            assertEquals(OutgoingChatMessageEntity.OutgoingStates.SENDING, createdMessage.getOutgoingState())

            receivedMessageLatch.countDown()
        }

        assertEquals(0, receivedMessageLatch.count)
    }

    @Test
    fun haveArgumentForMediaMessage_execute_receiveTextMessage() = runBlocking {
        val contentType = ChatMessageEntity.ContentTypes.MEDIA
        val dialogId = System.currentTimeMillis().toString()
        val content = System.currentTimeMillis().toString()

        val createdFileEntity = QuickBloxUiKit.getDependency().getFilesRepository().createLocalFile("pdf")
        createdFileEntity.getFile()!!.writeBytes("temp_pdf_file".toByteArray())
        val mimeType = createdFileEntity.getMimeType()

        val receivedMessageLatch = CountDownLatch(2)
        CreateMessageUseCase(contentType, dialogId, content, buildFileEntity()).execute().collect { createdMessage ->
            assertTrue(createdMessage?.geMessageId()!!.isNotEmpty())
            assertEquals(contentType, createdMessage.getContentType())
            assertEquals(dialogId, createdMessage.getDialogId())

            assertEquals(MediaContentEntity.Types.FILE, createdMessage.getMediaContent()!!.getType())
            assertTrue(createdMessage.getMediaContent()!!.getUrl().isNotEmpty())
            assertEquals(mimeType, createdMessage.getMediaContent()?.getMimeType())

            assertEquals(OutgoingChatMessageEntity.OutgoingStates.SENDING, createdMessage.getOutgoingState())

            receivedMessageLatch.countDown()
        }

        assertEquals(0, receivedMessageLatch.count)
    }

    private fun buildFileEntity(): FileEntity {
        val createdFileEntity = QuickBloxUiKit.getDependency().getFilesRepository().createLocalFile("pdf")
        createdFileEntity.getFile()!!.writeBytes("temp_pdf_file".toByteArray())

        return createdFileEntity
    }
}