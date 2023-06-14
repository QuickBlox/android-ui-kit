/*
 * Created by Injoit on 6.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StartTypingEventUseCase(private val dialogId: String) : BaseUseCase<Unit>() {
    private val dialogsRepository = QuickBloxUiKit.getDependency().getDialogsRepository()
    private val eventsRepository = QuickBloxUiKit.getDependency().getEventsRepository()

    override suspend fun execute() {
        if (dialogId.isEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        withContext(Dispatchers.IO) {
            runCatching {
                val dialog = getDialogBy(dialogId)
                eventsRepository.startTypingEvent(dialog)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }
    }

    private fun getDialogBy(dialogId: String): DialogEntity {
        return dialogsRepository.getDialogFromLocal(dialogId)
    }
}