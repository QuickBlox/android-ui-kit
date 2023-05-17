/*
 * Created by Injoit on 8.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetDialogsByNameUseCase(private val name: String) : BaseUseCase<Collection<DialogEntity>?>() {
    private val dialogRepository = QuickBloxUiKit.getDependency().getDialogsRepository()

    override suspend fun execute(): Collection<DialogEntity>? {
        if (name.isEmpty()) {
            throw DomainException("The name shouldn't be empty")
        }

        var foundDialogs: Collection<DialogEntity>? = null

        withContext(Dispatchers.IO) {
            runCatching {
                foundDialogs = dialogRepository.getDialogsByName(name)
            }.onFailure { error ->
                throw DomainException(error.message ?: DomainException.Codes.UNEXPECTED.toString())
            }
        }

        return foundDialogs
    }
}