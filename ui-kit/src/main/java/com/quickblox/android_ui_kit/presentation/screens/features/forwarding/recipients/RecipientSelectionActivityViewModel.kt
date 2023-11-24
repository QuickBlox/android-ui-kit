/*
 * Created by Injoit on 8.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.features.forwarding.recipients

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.CreateForwardMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendForwardMessageUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RecipientSelectionActivityViewModel : BaseViewModel() {
    private val _sentMessage = MutableLiveData<String>()
    val sentMessage: LiveData<String>
        get() = _sentMessage

    fun createAndSendMessage(
        forwardMessages: List<MessageEntity?>,
        dialog: DialogEntity?,
        text: String? = null,
    ) {

        viewModelScope.launch {
            try {
                var relatedMessage: OutgoingChatMessageEntity? = null
                var forwardedText = text

                if (text.isNullOrBlank()) {
                    forwardedText = "[Forwarded_Message]"
                }

                relatedMessage = CreateMessageUseCase(
                    ChatMessageEntity.ContentTypes.TEXT, dialog?.getDialogId().toString(), forwardedText
                ).execute().firstOrNull()
                forwardMessages as List<ForwardedRepliedMessageEntity>
                val forwardMessage = CreateForwardMessageUseCase(forwardMessages, relatedMessage).execute()

                forwardMessage?.let {
                    SendForwardMessageUseCase(it, dialog?.getDialogId().toString()).execute()
                    _sentMessage.postValue(dialog?.getName())
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }
}