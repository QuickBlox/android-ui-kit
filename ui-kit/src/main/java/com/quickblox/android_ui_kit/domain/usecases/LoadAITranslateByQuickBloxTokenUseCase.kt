/*
 * Created by Injoit on 8.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.exception.repository.AIRepositoryException
import com.quickblox.android_ui_kit.domain.usecases.base.BaseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExcludeFromCoverage
class LoadAITranslateByQuickBloxTokenUseCase(private val message: IncomingChatMessageEntity) :
    BaseUseCase<AITranslateIncomingChatMessageEntity?>() {
    private val usersRepository = QuickBloxUiKit.getDependency().getUsersRepository()
    private val aiRepository = QuickBloxUiKit.getDependency().getAIRepository()

    override suspend fun execute(): AITranslateIncomingChatMessageEntity? {
        var entity: AITranslateIncomingChatMessageEntity? = null

        withContext(Dispatchers.IO) {
            val token = usersRepository.getUserSessionToken()

            try {
                entity = aiRepository.translateIncomingMessageByQuickBloxToken(message, token)
            } catch (exception: AIRepositoryException) {
                throw DomainException(exception.message ?: "Unexpected Exception")
            }
        }

        return entity
    }
}