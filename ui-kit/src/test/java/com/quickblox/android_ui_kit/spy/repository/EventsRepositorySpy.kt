/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.spy.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.TypingEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.repository.EventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

open class EventsRepositorySpy() : EventsRepository {
    private val dialogsFlow = MutableSharedFlow<DialogEntity?>(0)
    private val messagesFlow = MutableSharedFlow<MessageEntity?>(0)
    private val typingFlow = MutableSharedFlow<Pair<Int?, TypingEntity.TypingTypes?>>(0)

    suspend fun sendDialog(dialogEntity: DialogEntity) {
        dialogsFlow.emit(dialogEntity)
    }

    suspend fun sendMessage(messageEntity: MessageEntity) {
        messagesFlow.emit(messageEntity)
    }

    suspend fun sendStartTyping(senderId: Int) {
        typingFlow.emit(Pair(senderId, TypingEntity.TypingTypes.STARTED))
    }

    suspend fun sendStopTyping(senderId: Int) {
        typingFlow.emit(Pair(senderId, TypingEntity.TypingTypes.STOPPED))
    }

    override fun startTypingEvent(dialogEntity: DialogEntity) {

    }

    override fun stopTypingEvent(dialogEntity: DialogEntity) {

    }

    override fun subscribeDialogEvents(): Flow<DialogEntity?> {
        return dialogsFlow
    }

    override fun subscribeMessageEvents(): Flow<MessageEntity?> {
        return messagesFlow
    }

    override fun subscribeTypingEvents(): Flow<Pair<Int?, TypingEntity.TypingTypes?>> {
        return typingFlow
    }
}