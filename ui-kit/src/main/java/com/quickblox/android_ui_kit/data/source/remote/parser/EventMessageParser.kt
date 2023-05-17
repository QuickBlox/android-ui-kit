/*
 * Created by Injoit on 27.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.source.remote.parser

import com.quickblox.chat.model.QBChatMessage

object EventMessageParser {
    const val PROPERTY_NOTIFICATION_TYPE = "notification_type"

    enum class EventTypes(val value: String) {
        CREATED_DIALOG("1"),
        ADDED_USER("2"),
        LEFT_USER("3"),
        REMOVED_USER("4")
    }

    fun isNotEventFrom(qbChatMessage: QBChatMessage): Boolean {
        return !isEventFrom(qbChatMessage)
    }

    fun isEventFrom(qbChatMessage: QBChatMessage): Boolean {
        val isCreateDialogEvent = isCreatedDialogEventFrom(qbChatMessage)
        val isAddedUserEvent = isAddedUserEventFrom(qbChatMessage)
        val isLeftUserEvent = isLeftUserEventFrom(qbChatMessage)
        val isRemovedUserEvent = isRemovedUserEventFrom(qbChatMessage)

        if (isCreateDialogEvent || isAddedUserEvent || isLeftUserEvent || isRemovedUserEvent) {
            return true
        }

        return false
    }

    fun isCreatedDialogEventFrom(qbChatMessage: QBChatMessage): Boolean {
        val parsedEvent = parseEventFrom(qbChatMessage)
        return parsedEvent == EventTypes.CREATED_DIALOG.value
    }

    fun isAddedUserEventFrom(qbChatMessage: QBChatMessage): Boolean {
        val parsedEvent = parseEventFrom(qbChatMessage)
        return parsedEvent == EventTypes.ADDED_USER.value
    }

    fun isLeftUserEventFrom(qbChatMessage: QBChatMessage): Boolean {
        val parsedEvent = parseEventFrom(qbChatMessage)
        return parsedEvent == EventTypes.LEFT_USER.value
    }

    fun isRemovedUserEventFrom(qbChatMessage: QBChatMessage): Boolean {
        val parsedEvent = parseEventFrom(qbChatMessage)
        return parsedEvent == EventTypes.REMOVED_USER.value
    }

    private fun parseEventFrom(qbChatMessage: QBChatMessage): String? {
        return qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) as String?
    }

    fun addCreatedDialogPropertyTo(qbChatMessage: QBChatMessage): QBChatMessage {
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, EventTypes.CREATED_DIALOG.value)

        return qbChatMessage
    }

    fun addAddedUsersPropertyTo(qbChatMessage: QBChatMessage): QBChatMessage {
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, EventTypes.ADDED_USER.value)

        return qbChatMessage
    }

    fun addRemovedUsersPropertyTo(qbChatMessage: QBChatMessage): QBChatMessage {
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, EventTypes.REMOVED_USER.value)

        return qbChatMessage
    }

    fun addLeftUsersPropertyTo(qbChatMessage: QBChatMessage): QBChatMessage {
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, EventTypes.LEFT_USER.value)

        return qbChatMessage
    }
}