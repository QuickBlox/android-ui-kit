/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIRephraseByQuickBloxTokenUseCase(private val toneEntity: AIRephraseToneEntity) :
    BaseUseCase<AIRephraseToneEntity?>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()

    override suspend fun execute(): AIRephraseToneEntity? {
        var resultEntity: AIRephraseToneEntity? = null

        withContext(Dispatchers.IO) {
            val token = usersRepository.getUserSessionToken()

            try {
                resultEntity = aiRepository.rephraseByQuickBloxToken(toneEntity, token)
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return resultEntity
    }
}