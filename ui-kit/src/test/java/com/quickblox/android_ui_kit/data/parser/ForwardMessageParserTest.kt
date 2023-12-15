/*
 * Created by Injoit on 16.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.parser

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.data.source.remote.parser.ForwardReplyMessageParser
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.spy.DependencySpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardMessageParserTest {
    @Test
    @ExperimentalCoroutinesApi
    fun correctJson_parseOutgoingMessagesFrom_receivedOutgoingMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val forwardedMessagesJsonArray = JSONArray(
            "[{\"_id\":\"6555bab4924a346ee0000005\",\"attachments\":[{\"id\":\"11411032\",\"uid\":\"7ebbafa088a342e689e8bc6a8f35f25300\",\"type\":\"image/png\",\"url\":\"https://api.quickblox.com/blobs/7ebbafa088a342e689e8bc6a8f35f25300?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdHlwZSI6InVzZXJfYWNjZXNzIiwiYXBwbGljYXRpb25faWQiOjc1OTQ5LCJhdWQiOiJxYl9jb3JlIiwiZXhwIjoxNzAwMjE3Njk0LCJpYXQiOjE3MDAxMzEyOTQsImlzcyI6InFiX2NvcmUiLCJqdGkiOiJhZjc5ZGYzNC1mMWRjLTQwNjItOTdhZC02ZGNmNTBlOWM1MDUiLCJuYmYiOjE3MDAxMzEyOTMsInN1YiI6MTM0ODA0MTQ3LCJ0eXAiOiJhY2Nlc3MifQ.3cXtP4SHfnT1U-XMUrCv4TjFwx-3DGdyROS8b3AIDvimwV_BDxW31tqwOQdTEHnzuFKJTPr2Ga81JsnQE-nzcg\",\"name\":\"game-icon-timber-2.png\",\"size\":\"29862\"}],\"chat_dialog_id\":\"6555ba1b32eaaf007c4f552d\",\"created_at\":\"2023-11-16T06:46:12Z\",\"date_sent\":1700117172000,\"delivered_ids\":[134804147],\"message\":\"game-icon-timber-2.png\",\"read_ids\":[134804147],\"read\":0,\"recipient_id\":0,\"sender_id\":134804147,\"updated_at\":\"2023-11-16T06:46:12Z\",\"notification_type\":\"\"}]"
        )

        val remoteDTO = RemoteMessageDTO()

        val properties = mutableMapOf<String?, String?>()
        properties[ForwardReplyMessageParser.QB_ORIGINAL_MESSAGES_KEY] = forwardedMessagesJsonArray.toString()
        remoteDTO.properties = properties

        val parsedOutgoingMessage = ForwardReplyMessageParser.parseOutgoingMessagesFrom(remoteDTO)
        parsedOutgoingMessage.toString()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun correctJson_parseIncomingMessagesFrom_receivedIncomingMessage() = runTest {
        QuickBloxUiKit.setDependency(DependencySpy())
        val forwardedMessagesJsonArray = JSONArray(
            "[{\"_id\":\"6555bab4924a346ee0000005\",\"attachments\":[{\"id\":\"11411032\",\"uid\":\"7ebbafa088a342e689e8bc6a8f35f25300\",\"type\":\"image/png\",\"url\":\"https://api.quickblox.com/blobs/7ebbafa088a342e689e8bc6a8f35f25300?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdHlwZSI6InVzZXJfYWNjZXNzIiwiYXBwbGljYXRpb25faWQiOjc1OTQ5LCJhdWQiOiJxYl9jb3JlIiwiZXhwIjoxNzAwMjE3Njk0LCJpYXQiOjE3MDAxMzEyOTQsImlzcyI6InFiX2NvcmUiLCJqdGkiOiJhZjc5ZGYzNC1mMWRjLTQwNjItOTdhZC02ZGNmNTBlOWM1MDUiLCJuYmYiOjE3MDAxMzEyOTMsInN1YiI6MTM0ODA0MTQ3LCJ0eXAiOiJhY2Nlc3MifQ.3cXtP4SHfnT1U-XMUrCv4TjFwx-3DGdyROS8b3AIDvimwV_BDxW31tqwOQdTEHnzuFKJTPr2Ga81JsnQE-nzcg\",\"name\":\"game-icon-timber-2.png\",\"size\":\"29862\"}],\"chat_dialog_id\":\"6555ba1b32eaaf007c4f552d\",\"created_at\":\"2023-11-16T06:46:12Z\",\"date_sent\":1700117172000,\"delivered_ids\":[134804147],\"message\":\"game-icon-timber-2.png\",\"read_ids\":[134804147],\"read\":0,\"recipient_id\":0,\"sender_id\":134804147,\"updated_at\":\"2023-11-16T06:46:12Z\",\"notification_type\":\"\"}]"
        )

        val remoteDTO = RemoteMessageDTO()

        val properties = mutableMapOf<String?, String?>()
        properties[ForwardReplyMessageParser.QB_ORIGINAL_MESSAGES_KEY] = forwardedMessagesJsonArray.toString()
        remoteDTO.properties = properties

        val parsedIncomingMessage = ForwardReplyMessageParser.parseIncomingMessagesFrom(remoteDTO)
        parsedIncomingMessage.toString()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun correctJsonAttachment_parseMediaContentFrom_receivedMediaContent() = runTest {
        val attachmentJson =
            JSONObject("{\"id\":\"11411032\",\"uid\":\"7ebbafa088a342e689e8bc6a8f35f25300\",\"type\":\"image/png\",\"url\":\"https://api.quickblox.com/blobs/7ebbafa088a342e689e8bc6a8f35f25300?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdHlwZSI6InVzZXJfYWNjZXNzIiwiYXBwbGljYXRpb25faWQiOjc1OTQ5LCJhdWQiOiJxYl9jb3JlIiwiZXhwIjoxNzAwMjE3Njk0LCJpYXQiOjE3MDAxMzEyOTQsImlzcyI6InFiX2NvcmUiLCJqdGkiOiJhZjc5ZGYzNC1mMWRjLTQwNjItOTdhZC02ZGNmNTBlOWM1MDUiLCJuYmYiOjE3MDAxMzEyOTMsInN1YiI6MTM0ODA0MTQ3LCJ0eXAiOiJhY2Nlc3MifQ.3cXtP4SHfnT1U-XMUrCv4TjFwx-3DGdyROS8b3AIDvimwV_BDxW31tqwOQdTEHnzuFKJTPr2Ga81JsnQE-nzcg\",\"name\":\"game-icon-timber-2.png\",\"size\":\"29862\"}")
        val mediaContent = ForwardReplyMessageParser.parseMediaContentFrom(attachmentJson)
        assertTrue(mediaContent.isImage())
        assertTrue(mediaContent.getUrl().isNotEmpty())
        assertTrue(mediaContent.getMimeType().isNotEmpty())
        assertTrue(mediaContent.getName().isNotEmpty())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun correctMediaContent_parseMediaContentToJson_receivedJson() = runTest {
        val fileUrl =
            "https://api.quickblox.com/blobs/7ebbafa088a342e689e8bc6a8f35f25300?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NfdHlwZSI6InVzZXJfYWNjZXNzIiwiYXBwbGljYXRpb25faWQiOjc1OTQ5LCJhdWQiOiJxYl9jb3JlIiwiZXhwIjoxNzAwMjE3Njk0LCJpYXQiOjE3MDAxMzEyOTQsImlzcyI6InFiX2NvcmUiLCJqdGkiOiJhZjc5ZGYzNC1mMWRjLTQwNjItOTdhZC02ZGNmNTBlOWM1MDUiLCJuYmYiOjE3MDAxMzEyOTMsInN1YiI6MTM0ODA0MTQ3LCJ0eXAiOiJhY2Nlc3MifQ.3cXtP4SHfnT1U-XMUrCv4TjFwx-3DGdyROS8b3AIDvimwV_BDxW31tqwOQdTEHnzuFKJTPr2Ga81JsnQE-nzcg"
        val mediaContent = MediaContentEntityImpl("test-icon", fileUrl, "image")
        val jsonMediaContent = ForwardReplyMessageParser.parseMediaContentToJson(mediaContent)
        assertTrue(jsonMediaContent.get("type").toString().isNotEmpty())
        assertTrue(jsonMediaContent.get("name").toString().isNotEmpty())
        assertTrue(jsonMediaContent.get("uid").toString().isNotEmpty())
    }
}