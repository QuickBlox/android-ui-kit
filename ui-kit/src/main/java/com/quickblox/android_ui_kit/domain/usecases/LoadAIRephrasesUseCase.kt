/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.AIRephraseToneEntityImpl
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAIRephrasesUseCase() : BaseUseCase<List<AIRephraseEntity>>() {
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()

    override suspend fun execute(): List<AIRephraseEntity> {
        val tones = mutableListOf<AIRephraseEntity>()

        withContext(Dispatchers.IO) {
            try {
                val result = aiRepository.getAllRephraseTones()
                tones.add(buildOriginalTone())
                result.forEach { tone ->
                    tones.add(AIRephraseEntityImpl(tone))
                }
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return tones
    }

    private fun buildOriginalTone(): AIRephraseEntityImpl {
        val tone = AIRephraseToneEntityImpl("Back to original text", "Original text", "\u2705")

        return AIRephraseEntityImpl(tone, AIRephraseEntityImpl.ToneType.ORIGINAL)
    }
}