/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.features.forwarding.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.PaginationEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.usecases.GetAllMessagesUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetDialogByIdUseCase
import com.quickblox.android_ui_kit.presentation.base.BaseViewModel
import com.quickblox.android_ui_kit.presentation.components.messages.DateHeaderMessageEntity
import com.quickblox.android_ui_kit.presentation.screens.modifyChatDateStringFrom
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MessagesSelectionViewModel : BaseViewModel() {
    private val TAG: String = MessagesSelectionViewModel::class.java.simpleName

    private var dialog: DialogEntity? = null
    private var pagination: PaginationEntity? = null
    var messages: MutableList<MessageEntity>? = null

    private var getMessagesUseCase: GetAllMessagesUseCase? = null
    private var getDialogByIdtJob: Job? = null
    private var loadMessagesJob: Job? = null

    private val _loadedMessage = MutableLiveData<Int>()
    val loadedMessage: LiveData<Int>
        get() = _loadedMessage

    fun setPaginationEntity(paginationEntity: PaginationEntity) {
        pagination = paginationEntity
    }

    fun setLoadedMessages(messages: MutableList<MessageEntity>?) {
        this.messages = messages
    }

    fun loadDialog(dialogId: String) {
        showLoading()
        if (getDialogByIdtJob?.isActive == true) {
            hideLoading()
            return
        }

        getDialogByIdtJob = viewModelScope.launch {
            runCatching {
                dialog = GetDialogByIdUseCase(dialogId).execute()
                hideLoading()
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

        val isNotExistNextPage = !pagination?.hasNextPage()!!
        if (loadMessagesJob?.isActive == true || isNotExistNextPage) {
            hideLoading()
            return
        }
        pagination?.nextPage()

        showLoading()
        loadMessagesJob = viewModelScope.launch {
            getMessagesUseCase = GetAllMessagesUseCase(dialog!!, pagination!!)
            getMessagesUseCase?.execute()?.onCompletion {
                hideLoading()

                val isHasNotNextPage = !pagination!!.hasNextPage()
                if (messages != null && messages?.isNotEmpty() == true && isHasNotNextPage) {
                    val lastMessage = messages!![messages?.size?.minus(1)!!]
                    addLastHeader(lastMessage)
                }
            }?.catch { error ->
                showError(error.message)
                hideLoading()
            }?.collect { result ->
                val message = result.getOrNull()?.first
                message?.let {
                    if (messages?.isNotEmpty() == true) {
                        val previousMessage = messages!![messages!!.size - 1]
                        if (isNeedAddHeaderBetweenMessages(message, previousMessage)) {
                            val time = previousMessage.getTime()
                            val dialogId = previousMessage.getDialogId()

                            val header = buildHeader(time, dialogId)
                            messages?.add(header)
                        }
                    }
                    addOrUpdateMessage(message)
                }
                pagination = result.getOrThrow().second
            }
        }
    }

    private fun addOrUpdateMessage(message: MessageEntity) {
        messages?.add(message)
        _loadedMessage.postValue(messages?.lastIndex)

    }

    private fun addLastHeader(lastMessage: MessageEntity) {
        if (lastMessage !is DateHeaderMessageEntity) {
            val time = lastMessage.getTime()
            val dialogId = lastMessage.getDialogId()

            val header = buildHeader(time, dialogId)
            messages?.add(header)
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

    override fun onConnected() {
        dialog?.getDialogId()?.let {
            loadDialog(it)
        }
    }
}