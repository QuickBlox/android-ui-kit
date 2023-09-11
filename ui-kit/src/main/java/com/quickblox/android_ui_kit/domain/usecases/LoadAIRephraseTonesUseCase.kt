/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseToneEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIRephraseTonesUseCase() : BaseUseCase<List<AIRephraseToneEntity>>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()

    override suspend fun execute(): List<AIRephraseToneEntity> {
        val tones = mutableListOf<AIRephraseToneEntity>()

        withContext(Dispatchers.IO) {
            try {
                val result = aiRepository.getAllRephraseTones()
                tones.add(buildOriginalTone())
                tones.addAll(result)
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return tones
    }

    private fun buildOriginalTone(): AIRephraseToneEntityImpl {
        return AIRephraseToneEntityImpl("Original", "\u2705", AIRephraseToneEntityImpl.ToneType.ORIGINAL)
    }
}