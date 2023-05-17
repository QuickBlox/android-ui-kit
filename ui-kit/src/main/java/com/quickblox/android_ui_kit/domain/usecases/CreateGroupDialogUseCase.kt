/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import androidx.annotation.VisibleForTesting
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.CustomDataEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.DialogEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.EventMessageEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateGroupDialogUseCase(
    private val name: String,
    private val participantIds: List<Int> = mutableListOf(),
    private val photoUrl: String? = null,
    private val customData: CustomDataEntity? = null
) : BaseUseCase<DialogEntity?>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val messagesRepository = QuickBloxUiKit.getDependency().getMessagesRepository()

    override suspend fun execute(): DialogEntity? {
        if (name.isEmpty()) {
            throw DomainException("The name shouldn't be empty")
        }

        var createdDialog: DialogEntity? = null

        withContext(Dispatchers.IO) {
            runCatching {
                createdDialog = dialogsRepository.createDialogInRemote(createDialog())

                sendEvent(createdDialog!!)

                createdDialog?.let { dialog ->
                    dialogsRepository.saveDialogToLocal(dialog)
                }
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return createdDialog
    }

    private fun sendEvent(dialog: DialogEntity) {
        val messageText = "The dialog $name was created"
        val event = createEvent(messageText, dialog.getDialogId()!!)

        messagesRepository.sendEventMessageToRemote(event, dialog)
    }

    @VisibleForTesting
    fun createDialog(): DialogEntity {
        val dialogEntity: DialogEntity = DialogEntityImpl()
        dialogEntity.setCustomData(customData)
        dialogEntity.setParticipantIds(participantIds)
        dialogEntity.setPhoto(photoUrl)
        dialogEntity.setName(name)
        dialogEntity.setDialogType(DialogEntity.Types.GROUP)

        return dialogEntity
    }

    @VisibleForTesting
    fun createEvent(text: String, dialogId: String): EventMessageEntity {
        val eventMessageEntity = EventMessageEntityImpl()
        eventMessageEntity.setText(text)
        eventMessageEntity.setEventType(EventMessageEntity.EventTypes.CREATED_DIALOG)
        eventMessageEntity.setDialogId(dialogId)
        eventMessageEntity.setTime(System.currentTimeMillis() / 1000)

        return eventMessageEntity
    }
}