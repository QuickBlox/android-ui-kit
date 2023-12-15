/*
 * Created by Injoit on 27.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.parser

import android.util.Log
import com.quickblox.android_ui_kit.data.dto.remote.message.RemoteMessageDTO
import com.quickblox.android_ui_kit.domain.entity.implementation.message.IncomingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.MediaContentEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.OutgoingChatMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object ForwardReplyMessageParser {
    private val TAG = ForwardReplyMessageParser::class.simpleName

    private const val QB_MESSAGE_ACTION_KEY = "qb_message_action"
    const val QB_ORIGINAL_MESSAGES_KEY = "qb_original_messages"

    //TODO: will be removed when we start support to forward multiple messages
    private const val ORIGIN_SENDER_NAME_KEY = "origin_sender_name"

    private const val MESSAGE_ID_KEY = "_id"
    private const val MESSAGE_CREATED_AT_KEY = "created_at"
    private const val MESSAGE_UPDATED_AT_KEY = "updated_at"
    private const val MESSAGE_DIALOG_ID_KEY = "chat_dialog_id"
    private const val MESSAGE_BODY_KEY = "message"
    private const val MESSAGE_DATE_SENT_KEY = "date_sent"
    private const val MESSAGE_SENDER_ID_KEY = "sender_id"
    private const val MESSAGE_RECIPIENT_ID_KEY = "recipient_id"
    private const val MESSAGE_READ_IDS_KEY = "read_ids"
    private const val MESSAGE_DELIVERED_IDS_KEY = "delivered_ids"
    private const val ATTACHMENTS_KEY = "attachments"

    private const val ATTACHMENT_NAME_KEY = "name"
    private const val ATTACHMENT_UID_KEY = "uid"
    private const val ATTACHMENT_TYPE_KEY = "type"

    private const val REPLY_TYPE = "reply"
    private const val FORWARD_TYPE = "forward"

    fun isForwardedOrRepliedIn(qbChatMessage: QBChatMessage): Boolean {
        return qbChatMessage.getProperty(QB_MESSAGE_ACTION_KEY) != null
    }

    fun parseForwardRepliedTypeFrom(dto: RemoteMessageDTO): ForwardedRepliedMessageEntity.Types {
        val parsedType = dto.properties?.get(QB_MESSAGE_ACTION_KEY)
        return if (parsedType == REPLY_TYPE) {
            ForwardedRepliedMessageEntity.Types.REPLIED
        } else {
            ForwardedRepliedMessageEntity.Types.FORWARDED
        }
    }

    fun parsePropertiesFrom(qbChatMessage: QBChatMessage): Map<String?, String?> {
        val properties: MutableMap<String?, String?> = mutableMapOf()
        properties[QB_MESSAGE_ACTION_KEY] = qbChatMessage.getProperty(QB_MESSAGE_ACTION_KEY) as String?
        properties[QB_ORIGINAL_MESSAGES_KEY] = qbChatMessage.getProperty(QB_ORIGINAL_MESSAGES_KEY) as String?

        //TODO: will be removed when we start support to forward multiple messages
        properties[ORIGIN_SENDER_NAME_KEY] = qbChatMessage.getProperty(ORIGIN_SENDER_NAME_KEY) as String?

        return properties
    }

    fun parseReplyPropertiesFrom(replyMessage: ChatMessageEntity): Map<String?, String?> {
        val properties = parsePropertiesFrom(listOf(replyMessage)) as MutableMap
        properties[QB_MESSAGE_ACTION_KEY] = REPLY_TYPE

        return properties
    }

    fun parseForwardPropertiesFrom(forwardMessages: List<ChatMessageEntity>): Map<String?, String?> {
        val properties = parsePropertiesFrom(forwardMessages) as MutableMap
        properties[QB_MESSAGE_ACTION_KEY] = FORWARD_TYPE

        return properties
    }

    private fun parsePropertiesFrom(messages: List<ChatMessageEntity>): Map<String?, String?> {
        val properties: MutableMap<String?, String?> = mutableMapOf()
        properties[QB_ORIGINAL_MESSAGES_KEY] = parseMessagesToJson(messages)
        properties[ORIGIN_SENDER_NAME_KEY] = messages[0].getSender()?.getName() ?: ""

        return properties
    }

    private fun parseMessagesToJson(messages: List<ChatMessageEntity>): String {
        val jsonArray = JSONArray()

        for (message in messages) {
            val messageInJson = parseMessageToJson(message)
            jsonArray.put(messageInJson)
        }

        return jsonArray.toString()
    }

    private fun parseMessageToJson(message: ChatMessageEntity): JSONObject {
        val json = JSONObject()
        json.put(MESSAGE_ID_KEY, message.getMessageId())
        json.put(MESSAGE_CREATED_AT_KEY, "")
        json.put(MESSAGE_UPDATED_AT_KEY, "")
        json.put(MESSAGE_DIALOG_ID_KEY, message.getDialogId())
        json.put(MESSAGE_BODY_KEY, message.getContent())
        json.put(MESSAGE_DATE_SENT_KEY, message.getTime())
        json.put(MESSAGE_SENDER_ID_KEY, message.getSenderId())
        json.put(MESSAGE_RECIPIENT_ID_KEY, message.getParticipantId())
        json.put(MESSAGE_READ_IDS_KEY, JSONArray())
        json.put(MESSAGE_DELIVERED_IDS_KEY, JSONArray())

        val isExistMediaContent = message.getMediaContent() != null
        if (isExistMediaContent) {
            val mediaContentJson = parseMediaContentToJson(message.getMediaContent())
            val mediaContentArrayJson = JSONArray().put(mediaContentJson)
            json.put(ATTACHMENTS_KEY, mediaContentArrayJson)
        }

        return json
    }

    fun parseMediaContentToJson(mediaContentEntity: MediaContentEntity?): JSONObject {
        val json = JSONObject()
        json.put(ATTACHMENT_TYPE_KEY, mediaContentEntity?.getType().toString().lowercase() ?: "")
        json.put(ATTACHMENT_NAME_KEY, mediaContentEntity?.getName() ?: "")

        val uid = parseUidFrom(mediaContentEntity?.getUrl())
        json.put(ATTACHMENT_UID_KEY, uid)

        return json
    }

    private fun parseUidFrom(fileUrl: String?): String {
        val urlWithoutToken = fileUrl?.substringBefore("?token=")
        val uid = urlWithoutToken?.substringAfter("/blobs/")
        return uid ?: ""
    }

    fun parseOutgoingMessagesFrom(dto: RemoteMessageDTO): List<OutgoingChatMessageEntity> {
        val parsedMessages = mutableListOf<OutgoingChatMessageEntity>()

        val jsonMessages = dto.properties?.get(QB_ORIGINAL_MESSAGES_KEY)
        if (jsonMessages?.isNotEmpty() == true) {

            val jsonArray = JSONArray(jsonMessages)
            for (index in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(index)
                try {
                    val chatMessageEntity = parseOutgoingMessageFrom(jsonObject, dto.id)
                    parsedMessages.add(chatMessageEntity)
                } catch (exception: ClassCastException) {
                    Log.d(TAG, "$exception")
                }
            }
        }

        return parsedMessages
    }

    private fun parseOutgoingMessageFrom(jsonObject: JSONObject, relatedMessageId: String?): OutgoingChatMessageEntity {
        val contentType = parseContentTypeFrom(jsonObject)
        val message = OutgoingChatMessageEntityImpl(OutgoingChatMessageEntity.OutgoingStates.SENDING, contentType)
        message.setRelatedMessageId(relatedMessageId)

        addValuesToChatMessageEntityFrom(message, jsonObject)

        return message
    }

    fun parseIncomingMessagesFrom(dto: RemoteMessageDTO): List<IncomingChatMessageEntity> {
        val parsedMessages = mutableListOf<IncomingChatMessageEntity>()

        val jsonMessages = dto.properties?.get(QB_ORIGINAL_MESSAGES_KEY)
        if (jsonMessages?.isNotEmpty() == true) {

            val jsonArray = JSONArray(jsonMessages)
            for (index in 0 until jsonArray.length()) {
                val messageInJson = jsonArray.getJSONObject(index)
                try {
                    val chatMessageEntity = parseIncomingMessageFrom(messageInJson, dto.id)
                    parsedMessages.add(chatMessageEntity)
                } catch (exception: ClassCastException) {
                    Log.d(TAG, "$exception")
                }
            }
        }

        return parsedMessages
    }

    private fun parseIncomingMessageFrom(jsonObject: JSONObject, relatedMessageId: String?): IncomingChatMessageEntity {
        val contentType = parseContentTypeFrom(jsonObject)
        val message = IncomingChatMessageEntityImpl(contentType)
        message.setRelatedMessageId(relatedMessageId)

        addValuesToChatMessageEntityFrom(message, jsonObject)

        return message
    }

    private fun parseContentTypeFrom(jsonObject: JSONObject): ChatMessageEntity.ContentTypes {
        val isMediaType = isJsonMessageHasMediaType(jsonObject)

        var contentType = ChatMessageEntity.ContentTypes.TEXT
        if (isMediaType) {
            contentType = ChatMessageEntity.ContentTypes.MEDIA
        }

        return contentType
    }

    private fun <T : ChatMessageEntity> addValuesToChatMessageEntityFrom(chatMessageEntity: T, jsonObject: JSONObject) {
        chatMessageEntity.setMessageId(getJsonValue<String>(jsonObject, MESSAGE_ID_KEY))
        chatMessageEntity.setDialogId(getJsonValue<String>(jsonObject, MESSAGE_DIALOG_ID_KEY))
        chatMessageEntity.setContent(getJsonValue<String>(jsonObject, MESSAGE_BODY_KEY))
        chatMessageEntity.setTime(getJsonValue<Number>(jsonObject, MESSAGE_DATE_SENT_KEY)?.toLong())
        chatMessageEntity.setSenderId(getJsonValue<Int>(jsonObject, MESSAGE_SENDER_ID_KEY))
        chatMessageEntity.setParticipantId(getJsonValue<Int>(jsonObject, MESSAGE_RECIPIENT_ID_KEY))

        val isMediaType = isJsonMessageHasMediaType(jsonObject)
        if (isMediaType) {
            val attachmentsJsonArray = jsonObject.get(ATTACHMENTS_KEY) as JSONArray
            val attachmentInJsonObject = attachmentsJsonArray[0] as JSONObject
            val mediaContentJson = parseMediaContentFrom(attachmentInJsonObject)
            chatMessageEntity.setMediaContent(mediaContentJson)
        }
    }

    private fun isJsonMessageHasMediaType(jsonObject: JSONObject): Boolean {
        val notExistAttachments = !jsonObject.has(ATTACHMENTS_KEY)
        if (notExistAttachments) {
            return false
        }

        try {
            val attachments = jsonObject.get(ATTACHMENTS_KEY) as JSONArray
            return attachments.length() > 0
        } catch (exception: JSONException) {
            return false
        } catch (exception: ClassCastException) {
            return false
        }
    }

    fun parseMediaContentFrom(jsonObject: JSONObject): MediaContentEntity {
        val fileName = getJsonValue<String>(jsonObject, ATTACHMENT_NAME_KEY) ?: ""

        val uid = getJsonValue<String>(jsonObject, ATTACHMENT_UID_KEY) ?: ""
        val fileUrl = QBFile.getPrivateUrlForUID(uid) ?: ""

        val type = getJsonValue<String>(jsonObject, ATTACHMENT_TYPE_KEY) ?: ""
        val mimeType = parseMimeType(type)

        val mediaContent = MediaContentEntityImpl(fileName, fileUrl, mimeType)

        return mediaContent
    }

    private fun parseMimeType(type: String): String {
        return type.split("/")[0]
    }

    private fun <T> getJsonValue(jsonObject: JSONObject, key: String): T? {
        try {
            return jsonObject.get(key) as T
        } catch (exception: JSONException) {
            return null
        } catch (exception: ClassCastException) {
            return null
        }
    }
}