/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class DialogEventUseCase(val dialogId: String) : FlowUseCase<DialogEntity?>() {
    private var eventsRepository = QuickBloxUiKit.getDependency().getEventsRepository()

    private var scope = CoroutineScope(Dispatchers.IO)
    private val dialogsEventFlow = MutableSharedFlow<DialogEntity?>(0)

    override suspend fun execute(): MutableSharedFlow<DialogEntity?> {
        if (dialogId.isEmpty()) {
            throw DomainException("The dialogId shouldn't be empty")
        }

        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        scope.launch {
            eventsRepository.subscribeDialogEvents().collect { dialogEntity ->
                if (dialogEntity?.getDialogId() == dialogId) {
                    dialogsEventFlow.emit(dialogEntity)
                }
            }
        }
        return dialogsEventFlow
    }

    override suspend fun release() {
        scope.cancel()
    }
}