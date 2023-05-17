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
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.implementation.PaginationEntityImpl
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ContentTypes
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.*
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import com.quickblox.android_ui_kit.presentation.components.messages.DateHeaderMessage
import com.quickblox.android_ui_kit.presentation.screens.modifyChatDateStringFrom
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GroupChatViewModel : BaseViewModel() {
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

    val messages = arrayListOf<MessageEntity>()
    private var loadMessagesJob: Job? = null
    private var subscribeMessagesEventJob: Job? = null

    private var pagination = createDefaultPagination()

    private val _loadedDialogEntity = MutableLiveData<DialogEntity?>()
    val loadedDialogEntity: LiveData<DialogEntity?>
        get() = _loadedDialogEntity

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
        if (lastMessage !is DateHeaderMessage) {
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

    private fun buildHeader(time: Long?, dialogId: String?): DateHeaderMessage {
        val timeString = modifyChatDateStringFrom(time ?: 0)
        return DateHeaderMessage(dialogId, timeString)
    }

    private fun isNeedAddHeaderBetweenMessages(message: MessageEntity, previousMessage: MessageEntity): Boolean {
        val isPreviousHeader = previousMessage is DateHeaderMessage
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
                messageEntity?.let {
                    addFirstOrUpdateMessage(it)
                }
            }
        }
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
                addFirstOrUpdateMessage(sentMessage)
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

                    addFirstOrUpdateMessage(message)
                    sendingMessage = message
                }
            } catch (exception: DomainException) {
                showError(exception.message)
            }
        }
    }

    private fun addFirstOrUpdateMessage(message: MessageEntity) {
        val index = messages.indexOf(message)
        val isNotFoundMessage = index == -1
        if (isNotFoundMessage) {
            addHeaderIsNeed(message)
            _receivedMessage.postValue(Unit)

            messages.add(0, message)
            _receivedMessage.postValue(Unit)
        } else {
            messages[index] = message
            _updatedMessage.postValue(index)
        }
    }

    private fun addHeaderIsNeed(message: MessageEntity) {
        if (messages.isNotEmpty() && isNeedAddHeaderBetweenMessages(messages[0], message)) {
            val time = message.getTime()
            val dialogId = message.getDialogId()

            val header = buildHeader(time, dialogId)
            messages.add(0, header)
        }
    }
}