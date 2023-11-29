/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.domain.entity.message.*
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ChatMessageTypes.FROM_LOGGED_USER
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ChatMessageTypes.FROM_OPPONENT
import com.quickblox.android_ui_kit.domain.entity.message.ChatMessageEntity.ContentTypes.TEXT
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.AIListener
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.MessageListener
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter.MessageViewHolderTypes.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.factory.MessageViewHolderFactory
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.factory.MessageViewHolderFactoryImpl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class MessageAdapter : RecyclerView.Adapter<BaseViewHolder<*>>() {
    enum class MessageViewHolderTypes(val code: Int) {
        DATE_HEADER(1),
        EVENT(2),
        TEXT_INCOMING(3),
        TEXT_OUTGOING(4),
        IMAGE_INCOMING(5),
        IMAGE_OUTGOING(6),
        AUDIO_INCOMING(7),
        AUDIO_OUTGOING(8),
        VIDEO_INCOMING(9),
        VIDEO_OUTGOING(10),
        FILE_INCOMING(11),
        FILE_OUTGOING(12)
    }

    private var theme: UiKitTheme = LightUIKitTheme()
    private var items: List<MessageEntity>? = null

    private var readMessageListener: ReadMessageListener? = null

    private var imageOutgoingListener: MessageListener? = null
    private var imageIncomingListener: MessageListener? = null
    private var textIncomingListener: MessageListener? = null
    private var aiListener: AIListener? = null
    private var textOutgoingListener: MessageListener? = null
    private var videoOutgoingListener: MessageListener? = null
    private var videoIncomingListener: MessageListener? = null
    private var fileOutgoingListener: MessageListener? = null
    private var fileIncomingListener: MessageListener? = null
    private var audioOutgoingListener: MessageListener? = null
    private var audioIncomingListener: MessageListener? = null
    private var forwardedClickListener: MessageListener? = null

    private var enabledAI: Boolean = false
    private var enabledAITranslation: Boolean = false

    private var isForwardState: Boolean = false
    private var lastCheckedHolder: BaseViewHolder<*>? = null

    private var selectedMessages = mutableListOf<MessageEntity>()
    private var selectionListener: SelectionListener? = null

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is BaseMessageViewHolder) {
            holder.clearCachedData()
        }
        val message = items?.get(position)
        holder.setTheme(theme)

        // TODO: Need to add one more interface in EventMessageEntity and IncomingMessageEntity and move delivered and read methods
        if (message is EventMessageEntity) {
            val isNotNullSenderId = message.getSenderId() != null
            if (message.isNotRead() && isNotNullSenderId) {
                readMessageListener?.read(message)
            }
        } else if (message is IncomingChatMessageEntity) {
            if (message.isNotRead()) {
                readMessageListener?.read(message)
            }
        }
        val isSelected =
            selectedMessages.contains(message) || message is ForwardedRepliedMessageEntity && !message.getForwardedRepliedMessages()
                .isNullOrEmpty() && selectedMessages.contains(
                message.getForwardedRepliedMessages()!![0]
            )

        if (lastCheckedHolder == null && isSelected) {
            lastCheckedHolder = holder
        }

        when (holder) {
            is DateHeaderViewHolder -> {
                message as DateHeaderMessageEntity
                holder.bind(message)
            }

            is EventViewHolder -> {
                message as EventMessageEntity
                holder.bind(message)
            }

            is TextIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.setShowAITranslate(enabledAITranslation)
                holder.bind(message, textIncomingListener, aiListener, isForwardState, selectedMessages)
                holder.setShowAIIcon(enabledAI)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is TextOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity

                holder.bind(message, textOutgoingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is ImageIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, imageIncomingListener, isForwardState, aiListener, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is ImageOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, imageOutgoingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is VideoOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, videoOutgoingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is VideoIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, videoIncomingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is FileOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, fileOutgoingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is FileIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, fileIncomingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is AudioOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, audioOutgoingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }

            is AudioIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, audioIncomingListener, isForwardState, selectedMessages)
                if (isForwardState) {
                    holder.setCheckBoxListener(MessageCheckBoxListener(holder, message))
                }
            }
        }
    }

    fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun setItems(items: List<MessageEntity>) {
        this.items = items
    }

    fun getItems(): List<MessageEntity>? {
        return items
    }

    fun getImageOutgoingListener(): MessageListener? {
        return imageOutgoingListener
    }

    fun setImageOutgoingListener(listener: MessageListener?) {
        this.imageOutgoingListener = listener
    }

    fun getForwardedClickListener(): MessageListener? {
        return forwardedClickListener
    }

    fun setForwardedClickListener(listener: MessageListener?) {
        this.forwardedClickListener = listener
    }

    fun getImageIncomingListener(): MessageListener? {
        return imageIncomingListener
    }

    fun setImageIncomingListener(listener: MessageListener?) {
        this.imageIncomingListener = listener
    }

    fun getTextIncomingListener(): MessageListener? {
        return textIncomingListener
    }

    fun setTextIncomingListener(listener: MessageListener?) {
        this.textIncomingListener = listener
    }

    fun setAIListener(aiListener: AIListener?) {
        this.aiListener = aiListener
    }

    fun getAIListener(): AIListener? {
        return aiListener
    }

    fun enabledAI(enabled: Boolean) {
        enabledAI = enabled
    }

    fun enabledAITranslate(enabled: Boolean) {
        enabledAITranslation = enabled
    }

    fun getTextOutgoingListener(): MessageListener? {
        return textOutgoingListener
    }

    fun setTextOutgoingListener(listener: MessageListener?) {
        this.textOutgoingListener = listener
    }

    fun getVideoOutgoingListener(): MessageListener? {
        return videoOutgoingListener
    }

    fun setVideoOutgoingListener(listener: MessageListener?) {
        this.videoOutgoingListener = listener
    }

    fun getVideoIncomingListener(): MessageListener? {
        return videoIncomingListener
    }

    fun setVideoIncomingListener(listener: MessageListener?) {
        this.videoIncomingListener = listener
    }

    fun getFileOutgoingListener(): MessageListener? {
        return fileOutgoingListener
    }

    fun setFileOutgoingListener(listener: MessageListener?) {
        this.fileOutgoingListener = listener
    }

    fun getFileIngoingListener(): MessageListener? {
        return fileIncomingListener
    }

    fun setFileIngoingListener(listener: MessageListener?) {
        this.fileIncomingListener = listener
    }

    fun getAudioOutgoingListener(): MessageListener? {
        return audioOutgoingListener
    }

    fun setAudioOutgoingListener(listener: MessageListener?) {
        this.audioOutgoingListener = listener
    }

    fun getAudioIncomingListener(): MessageListener? {
        return audioIncomingListener
    }

    fun setAudioIncomingListener(listener: MessageListener?) {
        this.audioIncomingListener = listener
    }

    fun getReadMessageListener(): ReadMessageListener? {
        return readMessageListener
    }

    fun setReadMessageListener(listener: ReadMessageListener?) {
        this.readMessageListener = listener
    }

    fun setForwardState(isForwardState: Boolean) {
        this.isForwardState = isForwardState
    }

    fun getSelectedMessages(): List<MessageEntity> {
        return selectedMessages
    }

    fun setSelectedMessages(message: MessageEntity) {
        selectedMessages.add(message)
    }

    fun setSelectionListener(selectionListener: SelectionListener) {
        this.selectionListener = selectionListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return createMessageViewHolderBy(viewType, parent)
    }

    private fun createMessageViewHolderBy(viewType: Int, parent: ViewGroup): BaseViewHolder<*> {
        val messageViewHolderFactory: MessageViewHolderFactory = MessageViewHolderFactoryImpl()
        return messageViewHolderFactory.createMessageViewHolder(viewType, parent)
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getMessageId().hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        val message = items?.get(position)

        if (isDateHeader(message)) {
            return DATE_HEADER.code
        }

        if (isEventMessage(message)) {
            return EVENT.code
        }

        if (isIncomingChatMessage(message)) {
            return getViewHolderTypeForIncoming(message)
        }

        if (isOutgoingChatMessage(message)) {
            return getViewHolderTypeForOutgoing(message)
        }

        throw RuntimeException("Message type does not exist")
    }

    private fun isIncomingChatMessage(message: MessageEntity?): Boolean {
        if (message is ChatMessageEntity) {
            return message.getChatMessageType() == FROM_OPPONENT
        }
        return false
    }

    private fun isOutgoingChatMessage(message: MessageEntity?): Boolean {
        if (message is ChatMessageEntity) {
            return message.getChatMessageType() == FROM_LOGGED_USER
        }
        return false
    }

    private fun isEventMessage(message: MessageEntity?): Boolean {
        return message?.getMessageType() == MessageEntity.MessageTypes.EVENT
    }

    private fun isDateHeader(message: MessageEntity?): Boolean {
        return message is DateHeaderMessageEntity
    }

    private fun getViewHolderTypeForIncoming(message: MessageEntity?): Int {
        message as ChatMessageEntity
        if (message.getContentType() == TEXT) {
            return TEXT_INCOMING.code
        } else {
            return parseIncomingHolderTypeBy(message.getMediaContent())
        }
    }

    private fun parseIncomingHolderTypeBy(mediaContent: MediaContentEntity?): Int {
        return when (mediaContent?.getType()) {
            MediaContentEntity.Types.IMAGE -> {
                IMAGE_INCOMING.code
            }
            MediaContentEntity.Types.AUDIO -> {
                AUDIO_INCOMING.code
            }
            MediaContentEntity.Types.VIDEO -> {
                VIDEO_INCOMING.code
            }
            MediaContentEntity.Types.FILE -> {
                FILE_INCOMING.code
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    private fun getViewHolderTypeForOutgoing(message: MessageEntity?): Int {
        message as ChatMessageEntity
        if (message.getContentType() == TEXT) {
            return TEXT_OUTGOING.code
        } else {
            return parseOutgoingHolderTypeBy(message.getMediaContent())
        }
    }

    private fun parseOutgoingHolderTypeBy(mediaContent: MediaContentEntity?): Int {
        return when (mediaContent?.getType()) {
            MediaContentEntity.Types.IMAGE -> {
                IMAGE_OUTGOING.code
            }
            MediaContentEntity.Types.AUDIO -> {
                AUDIO_OUTGOING.code
            }
            MediaContentEntity.Types.VIDEO -> {
                VIDEO_OUTGOING.code
            }
            MediaContentEntity.Types.FILE -> {
                FILE_OUTGOING.code
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    interface ReadMessageListener {
        fun read(message: MessageEntity)
    }

    interface CheckBoxListener {
        fun onSelected(selectedMessage: MessageEntity?)
        fun onUnselected(unSelectedMessage: MessageEntity?)
    }

    inner class MessageCheckBoxListener(private val holder: BaseViewHolder<*>, private val message: ChatMessageEntity) :
        CheckBoxListener {
        override fun onSelected(selectedMessage: MessageEntity?) {

            val checkedHolder = lastCheckedHolder

            if (checkedHolder is Forward && checkedHolder != holder) {
                checkedHolder.setChecked(false, selectedMessages)
            } else if (checkedHolder is Forward && checkedHolder == holder && selectedMessages.isNotEmpty()) {
                checkedHolder.setChecked(false, selectedMessages)
            }

            lastCheckedHolder = holder

            if (selectedMessage != null) {
                selectedMessages.add(selectedMessage)
            }
            notifySelectionListener(selectedMessages.size)
        }

        override fun onUnselected(unSelectedMessage: MessageEntity?) {
            selectedMessages.remove(unSelectedMessage)
            notifySelectionListener(selectedMessages.size)
        }
    }

    private fun notifySelectionListener(countMessages: Int) {
        selectionListener?.onSelection(countMessages)
    }

    interface SelectionListener {
        fun onSelection(countMessages: Int)
    }
}