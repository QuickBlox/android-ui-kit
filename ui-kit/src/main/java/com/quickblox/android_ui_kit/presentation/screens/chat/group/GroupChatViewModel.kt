/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.group

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ContentTypes
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.*
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import com.quickblox.android_ui_kit.presentation.components.messages.DateHeaderMessageEntity
import com.quickblox.android_ui_kit.presentation.screens.modifyChatDateStringFrom
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GroupChatViewModel : BaseViewModel() {
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

    private var pagination = createDefaultPagination()

    private val _loadedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedDialogEntity

    private val _rephrasedToneEntity = MutableLiveData<AIRephraseToneEntity>()
    val rephrasedText: LiveData<AIRephraseToneEntity>
        get() = _rephrasedToneEntity

    private val _allTones = MutableLiveData<List<AIRephraseToneEntity>>()
    val allTones: LiveData<List<AIRephraseToneEntity>>
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
                showError(error.message)
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

    fun createAndSendMessage(contentType: ContentTypes, text: String? = null, file: FileEntity? = null) {
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

    fun executeAIAnswerAssistant(dialogId: String, message: IncomingChatMessageEntity) {
        if (QuickBloxUiKit.isAIAnswerAssistantEnabledWithOpenAIToken()) {
            viewModelScope.launch {
                try {
                    showLoading()
                    val answers = LoadAIAnswerAssistantByOpenAITokenUseCase(dialogId, message).execute()
                    if (answers.isNotEmpty()) {
                        _aiAnswer.postValue(answers[0])
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
                    val answers = LoadAIAnswerAssistantByQuickBloxTokenUseCase(dialogId, message).execute()
                    if (answers.isNotEmpty()) {
                        _aiAnswer.postValue(answers[0])
                    }
                } catch (exception: DomainException) {
                    showError(exception.message)
                } finally {
                    hideLoading()
                }
            }
        }
    }

    fun executeAITranslation(message: IncomingChatMessageEntity) {
        if (message is AITranslateIncomingChatMessageEntity) {
            addOrUpdateMessage(message)
            return
        }

        if (QuickBloxUiKit.isAITranslateEnabledWithOpenAIToken()) {
            viewModelScope.launch {
                try {
                    val entity = LoadAITranslateByOpenAITokenUseCase(message).execute()
                    updateTranslatedMessage(entity)
                } catch (exception: DomainException) {
                    showError(exception.message)
                    addOrUpdateMessage(message)
                }
            }
        }
        if (QuickBloxUiKit.isAITranslateEnabledWithProxyServer()) {
            viewModelScope.launch {
                try {
                    val entity = LoadAITranslateByQuickBloxTokenUseCase(message).execute()
                    updateTranslatedMessage(entity)
                } catch (exception: DomainException) {
                    showError(exception.message)
                    addOrUpdateMessage(message)
                }
            }
        }
    }

    private fun updateTranslatedMessage(entity: AITranslateIncomingChatMessageEntity?) {
        if (entity?.getTranslations()?.isNotEmpty() == true) {
            entity.setTranslated(true)
            addOrUpdateMessage(entity)
        }
    }

    fun executeAIRephrase(toneEntity: AIRephraseToneEntity) {
        if (QuickBloxUiKit.isAIRephraseEnabledWithOpenAIToken()) {
            executeAIRephraseByOpenAIToken(toneEntity)
        }

        if (QuickBloxUiKit.isAIRephraseEnabledWithProxyServer()) {
            executeAIRephraseByQuickBloxToken(toneEntity)
        }
    }

    private fun executeAIRephraseByQuickBloxToken(toneEntity: AIRephraseToneEntity) {
        showLoading()
        viewModelScope.launch {
            try {
                val resultEntity = LoadAIRephraseByQuickBloxTokenUseCase(toneEntity).execute()
                resultEntity?.let {
                    _rephrasedToneEntity.postValue(resultEntity)
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            } finally {
                hideLoading()
            }
        }
    }

    private fun executeAIRephraseByOpenAIToken(toneEntity: AIRephraseToneEntity) {
        showLoading()
        viewModelScope.launch {
            try {
                val resultEntity = LoadAIRephraseByOpenAITokenUseCase(toneEntity).execute()
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
                val result = LoadAIRephraseTonesUseCase().execute()
                _allTones.postValue(result)
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    private fun isNeedSendDelivered(message: MessageEntity): Boolean {
        if (message is IncomingChatMessageEntity && message.isNotDelivered()) {
            return true
        }
        return false
    }
}