/*
 * Created by Injoit on 26.4.2023.
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
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter.MessageViewHolderTypes.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.AudioIncomingViewHolder.AudioIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.AudioOutgoingViewHolder.AudioOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.FileIncomingViewHolder.FileIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.FileOutgoingViewHolder.FileOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageIncomingViewHolder.ImageIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageOutgoingViewHolder.ImageOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.TextIncomingViewHolder.AIListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.TextIncomingViewHolder.TextIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.TextOutgoingViewHolder.TextOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.VideoIncomingViewHolder.VideoIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.VideoOutgoingViewHolder.VideoOutgoingListener
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

    private var imageOutgoingListener: ImageOutgoingListener? = null
    private var imageIncomingListener: ImageIncomingListener? = null
    private var textIncomingListener: TextIncomingListener? = null
    private var aiListener: AIListener? = null
    private var textOutgoingListener: TextOutgoingListener? = null
    private var videoOutgoingListener: VideoOutgoingListener? = null
    private var videoIncomingListener: VideoIncomingListener? = null
    private var fileOutgoingListener: FileOutgoingListener? = null
    private var fileIncomingListener: FileIncomingListener? = null
    private var audioOutgoingListener: AudioOutgoingListener? = null
    private var audioIncomingListener: AudioIncomingListener? = null

    private var enabledAI: Boolean = false

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
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
                holder.bind(message, textIncomingListener, aiListener)
                holder.setShowAIIcon(enabledAI)
            }

            is TextOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, textOutgoingListener)
            }

            is ImageIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, imageIncomingListener)
            }

            is ImageOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, imageOutgoingListener)
            }

            is VideoOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, videoOutgoingListener)
            }

            is VideoIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, videoIncomingListener)
            }

            is FileOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, fileOutgoingListener)
            }

            is FileIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, fileIncomingListener)
            }

            is AudioOutgoingViewHolder -> {
                message as OutgoingChatMessageEntity
                holder.bind(message, audioOutgoingListener)
            }

            is AudioIncomingViewHolder -> {
                message as IncomingChatMessageEntity
                holder.bind(message, audioIncomingListener)
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

    fun getImageOutgoingListener(): ImageOutgoingListener? {
        return imageOutgoingListener
    }

    fun setImageOutgoingListener(listener: ImageOutgoingListener?) {
        this.imageOutgoingListener = listener
    }

    fun getImageIncomingListener(): ImageIncomingListener? {
        return imageIncomingListener
    }

    fun setImageIncomingListener(listener: ImageIncomingListener?) {
        this.imageIncomingListener = listener
    }

    fun getTextIncomingListener(): TextIncomingListener? {
        return textIncomingListener
    }

    fun setTextIncomingListener(listener: TextIncomingListener?) {
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

    fun getTextOutgoingListener(): TextOutgoingListener? {
        return textOutgoingListener
    }

    fun setTextOutgoingListener(listener: TextOutgoingListener?) {
        this.textOutgoingListener = listener
    }

    fun getVideoOutgoingListener(): VideoOutgoingListener? {
        return videoOutgoingListener
    }

    fun setVideoOutgoingListener(listener: VideoOutgoingListener?) {
        this.videoOutgoingListener = listener
    }

    fun getVideoIncomingListener(): VideoIncomingListener? {
        return videoIncomingListener
    }

    fun setVideoIncomingListener(listener: VideoIncomingListener?) {
        this.videoIncomingListener = listener
    }

    fun getFileOutgoingListener(): FileOutgoingListener? {
        return fileOutgoingListener
    }

    fun setFileOutgoingListener(listener: FileOutgoingListener?) {
        this.fileOutgoingListener = listener
    }

    fun getFileIngoingListener(): FileIncomingListener? {
        return fileIncomingListener
    }

    fun setFileIngoingListener(listener: FileIncomingListener?) {
        this.fileIncomingListener = listener
    }

    fun getAudioOutgoingListener(): AudioOutgoingListener? {
        return audioOutgoingListener
    }

    fun setAudioOutgoingListener(listener: AudioOutgoingListener?) {
        this.audioOutgoingListener = listener
    }

    fun getAudioIncomingListener(): AudioIncomingListener? {
        return audioIncomingListener
    }

    fun setAudioIncomingListener(listener: AudioIncomingListener?) {
        this.audioIncomingListener = listener
    }

    fun getReadMessageListener(): ReadMessageListener? {
        return readMessageListener
    }

    fun setReadMessageListener(listener: ReadMessageListener?) {
        this.readMessageListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return createMessageViewHolderBy(viewType, parent)
    }

    private fun createMessageViewHolderBy(viewType: Int, parent: ViewGroup): BaseViewHolder<*> {
        val messageViewHolderFactory: MessageViewHolderFactory = MessageViewHolderFactoryImpl()
        return messageViewHolderFactory.createMessageViewHolder(viewType, parent)
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.geMessageId().hashCode().toLong()
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
}