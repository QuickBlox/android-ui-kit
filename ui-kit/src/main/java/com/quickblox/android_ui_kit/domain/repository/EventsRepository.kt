/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.repository

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.TypingEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import kotlinx.coroutines.flow.Flow

interface EventsRepository {
    fun startTypingEvent(dialogEntity: DialogEntity)

    fun stopTypingEvent(dialogEntity: DialogEntity)

    fun subscribeDialogEvents(): Flow<DialogEntity?>

    fun subscribeMessageEvents(): Flow<MessageEntity?>

    fun subscribeTypingEvents(): Flow<Pair<Int?, TypingEntity.TypingTypes?>>
}