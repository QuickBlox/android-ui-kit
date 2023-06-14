/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.usecases.base.FlowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class DialogsEventUseCase : FlowUseCase<DialogEntity?>() {
    private var eventsRepository = QuickBloxUiKit.getDependency().getEventsRepository()

    private var scope = CoroutineScope(Dispatchers.IO)
    private val dialogsEventFlow = MutableSharedFlow<DialogEntity?>(0)

    override suspend fun execute(): MutableSharedFlow<DialogEntity?> {
        if (isScopeNotActive(scope)) {
            scope = CoroutineScope(Dispatchers.IO)
        }

        scope.launch {
            eventsRepository.subscribeDialogEvents().collect { dialogEntity ->
                dialogEntity?.let {
                    dialogsEventFlow.emit(dialogEntity)
                }
            }
        }
        return dialogsEventFlow
    }

    @ExcludeFromCoverage
    override suspend fun release() {
        scope.cancel()
    }
}