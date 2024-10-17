/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.individual

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.CreateLocalFileUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.CreateReplyMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.DeliverMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetAllMessagesUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetLocalFileByUriUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIAnswerAssistantWithApiKeyUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIAnswerAssistantWithProxyServerUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIAnswerAssistantWithSmartChatAssistantIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIRephraseWithApiKeyUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIRephraseWithProxyServerUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAIRephrasesUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAITranslateWithApiKeyUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAITranslateWithProxyServerUseCase
import com.quickblox.android_ui_kit.domain.usecases.LoadAITranslateWithSmartChatAssistantIdUseCase
import com.quickblox.android_ui_kit.domain.usecases.MessagesEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.ReadMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendChatMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.SendForwardReplyMessageUseCase
import com.quickblox.android_ui_kit.domain.usecases.StartTypingEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.StopTypingEventUseCase
import com.quickblox.android_ui_kit.domain.usecases.TypingEventUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import com.quickblox.android_ui_kit.presentation.checkStringByRegex
import com.quickblox.android_ui_kit.presentation.components.messages.DateHeaderMessageEntity
import com.quickblox.android_ui_kit.presentation.screens.modifyChatDateStringFrom
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrivateChatViewModel : BaseViewModel() {
    enum class TypingEvents {
        STARTED, STOPPED
    }

    private val _typingEvents = MutableLiveData<Pair<TypingEvents, String>>()
    val typingEvents: LiveData<Pair<TypingEvents, String>>
        get() = _typingEvents

    private var dialog: DialogEntity? = null

    private val _loadedMessage = MutableLiveData<Int>()
    val loadedMessage: LiveData<Int>
        get() = _loadedMessage

    private val _receivedMessage = MutableLiveData<Unit>()
    val receivedMessage: LiveData<Unit>
        get() = _receivedMessage

    private val _updatedMessage = MutableLiveData<Int>()
    val updatedMessage: LiveData<Int>
        get() = _updatedMessage

    private val _aiAnswer = MutableLiveData<String>()
    val aiAnswer: LiveData<String>
        get() = _aiAnswer

    val messages = arrayListOf<MessageEntity>()
    private var loadMessagesJob: Job? = null
    private var subscribeMessagesEventJob: Job? = null

    var pagination = createDefaultPagination()

    private val _loadedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedDialogEntity

    private val _rephrasedToneEntity = MutableLiveData<AIRephraseEntity>()
    val rephrasedText: LiveData<AIRephraseEntity>
        get() = _rephrasedToneEntity

    private val _allTones = MutableLiveData<List<AIRephraseEntity>>()
    val allTones: LiveData<List<AIRephraseEntity>>
        get() = _allTones

    private var subscribeMessagesEventUseCase: MessagesEventUseCase? = null
    private var getMessagesUseCase: GetAllMessagesUseCase? = null
    private var getDialogByIdtJob: Job? = null

    init {
        subscribeConnection()
    }

    private fun createDefaultPagination(): PaginationEntity {
        return PaginationEntityImpl().apply {
            setPerPage(50)
            setCurrentPage(0)
            setHasNextPage(true)
        }
    }

    fun loadDialogAndMessages(dialogId: String) {
        showLoading()
        if (getDialogByIdtJob?.isActive == true) {
            hideLoading()
            return
        }

        getDialogByIdtJob = viewModelScope.launch {
            runCatching {
                dialog = GetDialogByIdUseCase(dialogId).execute()
                _loadedDialogEntity.postValue(dialog)
                loadMessages()
                subscribeToMessagesEvent()

                dialog?.let {
                    subscribeToTypingEvents(it)
                }
            }.onFailure { error ->
                hideLoading()
            }
        }
    }

    fun loadMessages() {
        if (dialog == null) {
            hideLoading()
            showError("DialogEntity should not be null")
            return
        }

        val isNotExistNextPage = !pagination.hasNextPage()
        if (loadMessagesJob?.isActive == true || isNotExistNextPage) {
            hideLoading()
            return
        }
        pagination.nextPage()

        showLoading()
        loadMessagesJob = viewModelScope.launch {
            getMessagesUseCase = GetAllMessagesUseCase(dialog!!, pagination)
            getMessagesUseCase?.execute()?.onCompletion {
                hideLoading()
                messages.sortByDescending { it.getTime() }

                val isHasNotNextPage = !pagination.hasNextPage()
                if (messages.isNotEmpty() && isHasNotNextPage) {
                    val lastMessage = messages[messages.size - 1]
                    addLastHeader(lastMessage)
                }
            }?.catch { error ->
                showError(error.message)
                hideLoading()
            }?.collect { result ->
                val message = result.getOrNull()?.first
                message?.let {
                    if (isNeedSendDelivered(message)) {
                        sendDelivered(message)
                    }

                    if (message is IncomingChatMessageEntity) {
                        val regexUserName = QuickBloxUiKit.getRegexUserName()
                        if (regexUserName != null) {
                            val sender = message.getSender()
                            checkSenderNameByRegex(sender, regexUserName)
                        }
                    }

                    if (messages.isNotEmpty()) {
                        val previousMessage = messages[messages.size - 1]
                        if (isNeedAddHeaderBetweenMessages(message, previousMessage)) {
                            val time = previousMessage.getTime()
                            val dialogId = previousMessage.getDialogId()

                            val header = buildHeader(time, dialogId)
                            messages.add(header)
                        }
                    }
                    addOrUpdateMessage(message)
                }
                pagination = result.getOrThrow().second
            }
        }
    }

    private fun checkSenderNameByRegex(sender: UserEntity?, regexUserName: String) {
        val isUserNameValid = sender?.getName()?.checkStringByRegex(regexUserName)
        if (isUserNameValid == false) {
            sender.setName("Unknown")
        }
    }

    private fun addLastHeader(lastMessage: MessageEntity) {
        if (lastMessage !is DateHeaderMessageEntity) {
            val time = lastMessage.getTime()
            val dialogId = lastMessage.getDialogId()

            val header = buildHeader(time, dialogId)
            messages.add(header)
        }
    }

    private fun addOrUpdateMessage(message: MessageEntity) {
        val index = messages.indexOf(message)

        val isNotFoundMessage = index == -1
        if (isNotFoundMessage) {
            messages.add(message)
            _loadedMessage.postValue(messages.lastIndex)
        } else {
            messages[index] = message
            _updatedMessage.postValue(index)
        }
    }

    private fun buildHeader(time: Long?, dialogId: String?): DateHeaderMessageEntity {
        val timeString = modifyChatDateStringFrom(time ?: 0)
        return DateHeaderMessageEntity(dialogId, timeString)
    }

    private fun isNeedAddHeaderBetweenMessages(message: MessageEntity, previousMessage: MessageEntity): Boolean {
        val isPreviousHeader = previousMessage is DateHeaderMessageEntity
        if (isPreviousHeader) {
            return false
        }

        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())

        val messageTimeInMilliseconds: Long = (message.getTime() ?: 0) * 1000
        val messageDate = dateFormat.format(Date(messageTimeInMilliseconds)).toLong()

        val previousMessageTimeInMilliseconds: Long = (previousMessage.getTime() ?: 0) * 1000
        val previousMessageDate = dateFormat.format(Date(previousMessageTimeInMilliseconds)).toLong()

        return messageDate < previousMessageDate
    }

    private fun subscribeToMessagesEvent() {
        if (dialog == null) {
            showError("DialogEntity should not be null")
            hideLoading()
            return
        }

        if (subscribeMessagesEventJob?.isActive == true) {
            hideLoading()
            return
        }

        subscribeMessagesEventJob = viewModelScope.launch {
            subscribeMessagesEventUseCase = MessagesEventUseCase(dialog!!)
            subscribeMessagesEventUseCase?.execute()?.collect { messageEntity ->
                messageEntity?.let { message ->
                    if (message is IncomingChatMessageEntity) {
                        val regexUserName = QuickBloxUiKit.getRegexUserName()
                        if (regexUserName != null) {
                            val sender = message.getSender()
                            checkSenderNameByRegex(sender, regexUserName)
                        }
                    }

                    if (isExistMessage(message)) {
                        updatedMessage(message)
                        return@let
                    }

                    if (isDelivered(message) || isRead(message)) {
                        return@let
                    }

                    if (isNeedAddHeaderBeforeFirst(message)) {
                        addHeaderBeforeFirst(message)
                    }

                    addAsFirst(message)
                }
            }
        }
    }

    private fun isDelivered(message: MessageEntity): Boolean {
        return message is OutgoingChatMessageEntity &&
                message.getOutgoingState() == OutgoingChatMessageEntity.OutgoingStates.DELIVERED
    }

    private fun isRead(message: MessageEntity): Boolean {
        return message is OutgoingChatMessageEntity &&
                message.getOutgoingState() == OutgoingChatMessageEntity.OutgoingStates.READ
    }

    override fun onConnected() {
        dialog?.getDialogId()?.let {
            loadDialogAndMessages(it)
        }
    }

    override fun onDisconnected() {
        pagination = createDefaultPagination()

        viewModelScope.launch {
            getMessagesUseCase?.release()
            subscribeMessagesEventUseCase?.release()
            loadMessagesJob?.cancel()
            subscribeMessagesEventJob?.cancel()
        }
    }

    private fun sendMessage(message: OutgoingChatMessageEntity) {
        viewModelScope.launch {
            runCatching {
                val sentMessage = SendChatMessageUseCase(message).execute()

                if (isExistMessage(sentMessage)) {
                    updatedMessage(sentMessage)
                    return@launch
                }

                if (isNeedAddHeaderBeforeFirst(sentMessage)) {
                    addHeaderBeforeFirst(sentMessage)
                }

                addAsFirst(sentMessage)
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    suspend fun createFileWith(extension: String): FileEntity? {
        try {
            val fileEntity = CreateLocalFileUseCase(extension).execute()
            return fileEntity
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun getFileBy(uri: Uri): FileEntity? {
        try {
            val file = GetLocalFileByUriUseCase(uri).execute()
            return file
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    fun createAndSendReplyMessage(
        repliedMessage: ForwardedRepliedMessageEntity?,
        contentType: ChatMessageEntity.ContentTypes,
        text: String? = null,
        file: FileEntity? = null,
    ) {
        if (repliedMessage == null || dialog?.getDialogId().isNullOrEmpty()) {
            return
        }
        val dialogId = dialog?.getDialogId()!!
        var relatedMessage: OutgoingChatMessageEntity? = null

        viewModelScope.launch {
            try {
                relatedMessage = CreateMessageUseCase(contentType, dialogId, text, file).execute().lastOrNull()
                val replyMessage = relatedMessage?.let {
                    CreateReplyMessageUseCase(repliedMessage, it).execute()
                }

                replyMessage as MessageEntity
                if (isNeedAddHeaderBeforeFirst(replyMessage)) {
                    addHeaderBeforeFirst(replyMessage)
                }

                addAsFirst(replyMessage)
                sendReplyMessage(replyMessage)
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    private fun sendReplyMessage(replyMessage: OutgoingChatMessageEntity) {
        viewModelScope.launch {
            runCatching {
                val sentMessage =
                    SendForwardReplyMessageUseCase(replyMessage, dialog?.getDialogId().toString()).execute()

                if (isExistMessage(sentMessage)) {
                    updatedMessage(sentMessage)
                    return@launch
                }

                if (isNeedAddHeaderBeforeFirst(sentMessage)) {
                    addHeaderBeforeFirst(sentMessage)
                }

                addAsFirst(sentMessage)
            }.onFailure { error ->
                showError(error.message)
            }
        }
    }

    fun createAndSendMessage(
        contentType: ChatMessageEntity.ContentTypes,
        text: String? = null,
        file: FileEntity? = null,
    ) {
        if (dialog?.getDialogId().isNullOrEmpty()) {
            return
        }

        var sendingMessage: OutgoingChatMessageEntity? = null
        val dialogId = dialog?.getDialogId()!!
        viewModelScope.launch {
            try {
                CreateMessageUseCase(contentType, dialogId, text, file).execute().onCompletion {
                    sendingMessage?.let { message ->
                        sendMessage(message)
                    }
                }.collect { message ->
                    message as MessageEntity

                    if (isExistMessage(message)) {
                        updatedMessage(message)
                        return@collect
                    }

                    if (isNeedAddHeaderBeforeFirst(message)) {
                        addHeaderBeforeFirst(message)
                    }

                    addAsFirst(message)
                    sendingMessage = message
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    private fun isExistMessage(message: MessageEntity): Boolean {
        val index = messages.indexOf(message)
        return index != -1
    }

    private fun isNeedAddHeaderBeforeFirst(message: MessageEntity): Boolean {
        return messages.isNotEmpty() && isNeedAddHeaderBetweenMessages(messages[0], message)
    }

    // TODO: Need to refactor when we will have message cache
    private fun updatedMessage(newMessage: MessageEntity) {
        val index = messages.indexOf(newMessage)
        val oldMessage = messages[index]
        if (oldMessage is OutgoingChatMessageEntity && newMessage is OutgoingChatMessageEntity) {
            oldMessage.setOutgoingState(newMessage.getOutgoingState())
        }
        _updatedMessage.postValue(index)
    }

    private fun addHeaderBeforeFirst(message: MessageEntity) {
        val time = message.getTime()
        val dialogId = message.getDialogId()

        val header = buildHeader(time, dialogId)
        messages.add(0, header)

        _receivedMessage.postValue(Unit)
    }

    private fun addAsFirst(message: MessageEntity) {
        messages.add(0, message)
        _receivedMessage.postValue(Unit)
    }

    fun sendRead(message: MessageEntity) {
        viewModelScope.launch {
            runCatching {
                ReadMessageUseCase(message).execute()
            }.onFailure {
                showError(it.message)
            }
        }
    }

    private fun sendDelivered(message: MessageEntity) {
        viewModelScope.launch {
            runCatching {
                DeliverMessageUseCase(message).execute()
            }.onFailure {
                showError(it.message)
            }
        }
    }

    private fun subscribeToTypingEvents(dialogEntity: DialogEntity) {
        viewModelScope.launch {
            viewModelScope.launch {
                TypingEventUseCase(dialogEntity).execute().runCatching {
                    collect { typingEntity ->
                        if (typingEntity?.isStarted() == true) {
                            _typingEvents.postValue(Pair(TypingEvents.STARTED, typingEntity.getText()))
                        } else if (typingEntity?.isStopped() == true) {
                            _typingEvents.postValue(Pair(TypingEvents.STOPPED, ""))
                        }
                    }
                }.onFailure {
                    showError(it.message)
                }
            }
        }
    }

    fun sendStartedTyping() {
        viewModelScope.launch {
            dialog?.getDialogId()?.let {
                runCatching {
                    StartTypingEventUseCase(it).execute()
                }.onFailure {
                    showError(it.message)
                }
            }
        }
    }

    fun sendStoppedTyping() {
        viewModelScope.launch {
            dialog?.getDialogId()?.let {
                runCatching {
                    StopTypingEventUseCase(it).execute()
                }.onFailure {
                    showError(it.message)
                }
            }
        }
    }

    fun executeAIAnswerAssistant(dialogId: String, message: ForwardedRepliedMessageEntity) {
        if (QuickBloxUiKit.isAIAnswerAssistantEnabledWithSmartChatAssistantId()) {
            viewModelScope.launch {
                try {
                    showLoading()
                    val answer = LoadAIAnswerAssistantWithSmartChatAssistantIdUseCase(dialogId, message).execute()
                    if (answer.isNotEmpty()) {
                        _aiAnswer.postValue(answer)
                    }
                } catch (exception: DomainException) {
                    showError(exception.message)
                } finally {
                    hideLoading()
                }
            }
        }

        if (QuickBloxUiKit.isAIAnswerAssistantEnabledWithOpenAIToken()) {
            viewModelScope.launch {
                try {
                    showLoading()
                    val answer = LoadAIAnswerAssistantWithApiKeyUseCase(dialogId, message).execute()
                    if (answer.isNotEmpty()) {
                        _aiAnswer.postValue(answer)
                    }
                } catch (exception: DomainException) {
                    showError(exception.message)
                } finally {
                    hideLoading()
                }
            }
        }
        if (QuickBloxUiKit.isAIAnswerAssistantEnabledWithProxyServer()) {
            viewModelScope.launch {
                try {
                    showLoading()
                    val answer = LoadAIAnswerAssistantWithProxyServerUseCase(dialogId, message).execute()
                    if (answer.isNotEmpty()) {
                        _aiAnswer.postValue(answer)
                    }
                } catch (exception: DomainException) {
                    showError(exception.message)
                } finally {
                    hideLoading()
                }
            }
        }
    }

    fun executeAITranslate(message: ForwardedRepliedMessageEntity) {
        if (message is AITranslateIncomingChatMessageEntity) {
            val reverseIsTranslated = !message.isTranslated()
            message.setTranslated(reverseIsTranslated)

            var updateMessage = message

            val isInternalMessage = !message.getRelatedMessageId().isNullOrEmpty()
            if (isInternalMessage) {
                updateMessage = findMessageBy(message.getRelatedMessageId())
            }
            addOrUpdateMessage(updateMessage)
            return
        }

        if (QuickBloxUiKit.isAITranslateEnabledWithSmartChatAssistantId()) {
            viewModelScope.launch {
                try {
                    var updateMessage: MessageEntity
                    val entity =
                        LoadAITranslateWithSmartChatAssistantIdUseCase(dialog?.getDialogId(), message).execute()!!
                    entity.setTranslated(true)
                    updateMessage = entity

                    val isInternalMessage = !message.getRelatedMessageId().isNullOrEmpty()
                    if (isInternalMessage) {
                        val foundMessage = findMessageBy(entity.getRelatedMessageId())
                        foundMessage.setForwardedRepliedMessages(listOf(entity as ForwardedRepliedMessageEntity))
                        updateMessage = foundMessage
                    }

                    addOrUpdateMessage(updateMessage)
                } catch (exception: DomainException) {
                    showError(exception.message)
                    addOrUpdateMessage(message)
                }
            }
        }

        if (QuickBloxUiKit.isAITranslateEnabledWithOpenAIToken()) {
            viewModelScope.launch {
                try {
                    var updateMessage: MessageEntity
                    val entity = LoadAITranslateWithApiKeyUseCase(dialog?.getDialogId(), message).execute()!!
                    entity.setTranslated(true)
                    updateMessage = entity

                    val isInternalMessage = !message.getRelatedMessageId().isNullOrEmpty()
                    if (isInternalMessage) {
                        val foundMessage = findMessageBy(entity.getRelatedMessageId())
                        foundMessage.setForwardedRepliedMessages(listOf(entity as ForwardedRepliedMessageEntity))
                        updateMessage = foundMessage
                    }

                    addOrUpdateMessage(updateMessage)
                } catch (exception: DomainException) {
                    showError(exception.message)
                    addOrUpdateMessage(message)
                }
            }
        }
        if (QuickBloxUiKit.isAITranslateEnabledWithProxyServer()) {
            viewModelScope.launch {
                try {
                    var updateMessage: MessageEntity
                    val entity = LoadAITranslateWithProxyServerUseCase(dialog?.getDialogId(), message).execute()!!
                    entity.setTranslated(true)
                    updateMessage = entity

                    val isInternalMessage = !message.getRelatedMessageId().isNullOrEmpty()
                    if (isInternalMessage) {
                        val foundMessage = findMessageBy(entity.getRelatedMessageId())
                        foundMessage.setForwardedRepliedMessages(listOf(entity as ForwardedRepliedMessageEntity))
                        updateMessage = foundMessage
                    }

                    addOrUpdateMessage(updateMessage)
                } catch (exception: DomainException) {
                    showError(exception.message)
                    addOrUpdateMessage(message)
                }
            }
        }
    }

    private fun findMessageBy(messageId: String?): ForwardedRepliedMessageEntity {
        return messages.find {
            it.getMessageId() == messageId
        } as ForwardedRepliedMessageEntity
    }

    fun executeAIRephrase(toneEntity: AIRephraseEntity) {
        if (QuickBloxUiKit.isAIRephraseEnabledWithProxyServer()) {
            executeAIRephraseByQuickBloxToken(toneEntity)
        }

        if (QuickBloxUiKit.isAIRephraseEnabledWithOpenAIToken()) {
            executeAIRephraseByOpenAIToken(toneEntity)
        }
    }

    private fun executeAIRephraseByQuickBloxToken(toneEntity: AIRephraseEntity) {
        showLoading()
        viewModelScope.launch {
            try {
                val resultEntity = LoadAIRephraseWithProxyServerUseCase(dialog?.getDialogId(), toneEntity).execute()
                resultEntity?.let {
                    _rephrasedToneEntity.postValue(it)
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            } finally {
                hideLoading()
            }
        }
    }

    private fun executeAIRephraseByOpenAIToken(toneEntity: AIRephraseEntity) {
        showLoading()
        viewModelScope.launch {
            try {
                val resultEntity = LoadAIRephraseWithApiKeyUseCase(dialog?.getDialogId(), toneEntity).execute()
                resultEntity?.let {
                    _rephrasedToneEntity.postValue(it)
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            } finally {
                hideLoading()
            }
        }
    }

    fun getAllTones() {
        viewModelScope.launch {
            try {
                val result = LoadAIRephrasesUseCase().execute()
                _allTones.postValue(result)
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    fun isNeedSendDelivered(message: MessageEntity): Boolean {
        if (message is IncomingChatMessageEntity && message.isNotDelivered()) {
            return true
        }
        return false
    }
}