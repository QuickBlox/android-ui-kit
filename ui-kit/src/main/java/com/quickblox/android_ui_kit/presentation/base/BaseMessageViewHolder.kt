/*
 * Created by Injoit on 15.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.*
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.*
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

abstract class BaseMessageViewHolder<VB : ViewBinding>(viewBinding: VB) : BaseViewHolder<VB>(viewBinding) {
    private var selectedMessages: List<MessageEntity>? = null
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null
    private var forwardedCheckBox: CheckBox? = null

    protected fun setSelectedMessages(selectedMessages: MutableList<MessageEntity>) {
        this.selectedMessages = selectedMessages
    }

    abstract fun clearCachedData()

    protected fun buildOutgoingMessage(
        message: ForwardedRepliedMessageEntity?,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
    ): View {
        val forwardedMessage = message?.getForwardedRepliedMessages()?.get(0)
        return if (forwardedMessage?.getContentType() == ChatMessageEntity.ContentTypes.TEXT) {
            buildTextOutgoingMessage(message, listener, theme, isForwardState)
        } else {
            buildMediaOutgoingMessage(message, listener, theme, isForwardState)
        }
    }

    protected fun buildIncomingMessage(
        message: IncomingChatMessageEntity?,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
        aiListener: AIListener?
    ): View? {
        val forwardedMessage = message?.getForwardedRepliedMessages()?.get(0)
        return if (forwardedMessage?.getContentType() == ChatMessageEntity.ContentTypes.TEXT) {
            buildTextIncomingMessage(message, listener, theme, isForwardState, aiListener)
        } else {
            buildMediaIncomingMessage(message, listener, theme, isForwardState)
        }
    }

    open fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildTextIncomingMessage(
        message: IncomingChatMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
        aiListener: AIListener?,
    ): View {
        val textMessageBinding = buildForwardedReplyIncomingTextMessageBinding()
        val textMessage = message.getForwardedRepliedMessages()?.get(0)

        val sender = textMessage?.getSender()
        textMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        if (message.isForwarded()) {
            showForwardHeader(textMessageBinding.ivIcon, textMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(textMessageBinding.ivIcon, textMessageBinding.tvActionText)
            textMessageBinding.tvTime.visibility = View.INVISIBLE
        }

        textMessageBinding.tvMessage.background =
            AppCompatResources.getDrawable(textMessageBinding.tvMessage.context, R.drawable.bg_forwarded_message)
        textMessageBinding.tvMessage.setBackgroundTintList(ColorStateList.valueOf(theme.getIncomingMessageColor()))

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        textMessageBinding.ivAvatar.setImageDrawable(avatarHolder)

        textMessageBinding.tvName.setTextColor(theme.getTertiaryElementsColor())
        textMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        textMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        textMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        textMessageBinding.tvMessage.setTextColor(theme.getMainTextColor())
        textMessageBinding.tvAITranslate.setTextColor(theme.getTertiaryElementsColor())
        textMessageBinding.progressAI.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
        textMessageBinding.ivAI.setColorFilter(theme.getMainElementsColor())
        textMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        if (QuickBloxUiKit.isEnabledAITranslate() && textMessage is AITranslateIncomingChatMessageEntity) {
            updateTranslatedContent(textMessage, textMessageBinding)
        } else {
            textMessageBinding.tvMessage.text = textMessage?.getContent()
        }

        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            textMessageBinding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        textMessageBinding.tvMessage.setOnLongClickListener {
            true
        }

        textMessageBinding.tvMessage.setOnTouchListener(
            TouchListener(textMessageBinding.tvMessage.context, textMessage, listener, textMessageBinding.tvMessage)
        )

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        textMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        if (isForwardState) {
            textMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (textMessage?.getRelatedMessageId() != null && textMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    textMessageBinding.checkbox.isChecked = true
                }
            }
            textMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(textMessage)
                } else {
                    checkBoxListener?.onUnselected(textMessage)
                }
            }

            forwardedCheckBox = textMessageBinding.checkbox
        } else {
            setAIListener(message, aiListener, textMessageBinding)
            setTranslateListener(textMessage!!, aiListener, textMessageBinding)

        }

        if (QuickBloxUiKit.isEnabledAITranslate()) {
            textMessageBinding.tvAITranslate.visibility = View.VISIBLE
        } else {
            textMessageBinding.tvAITranslate.visibility = View.GONE
        }

        if (QuickBloxUiKit.isEnabledAIAnswerAssistant()) {
            textMessageBinding.ivAI.visibility = View.VISIBLE
        } else {
            textMessageBinding.ivAI.visibility = View.GONE
        }

        return textMessageBinding.root
    }

    private fun showForwardHeader(ivIcon: AppCompatImageView, tvActionText: AppCompatTextView) {
        ivIcon.setImageResource(R.drawable.ic_forward)
        tvActionText.text = tvActionText.context.getString(R.string.forwarded_from)

    }

    private fun showReplyHeader(ivIcon: AppCompatImageView, tvActionText: AppCompatTextView) {
        ivIcon.setImageResource(R.drawable.ic_reply)
        tvActionText.text = tvActionText.context.getString(R.string.replied_to)
    }

    private fun updateTranslatedContent(
        message: AITranslateIncomingChatMessageEntity,
        textMessageBinding: ForwardedReplyIncomingTextMessageBinding,
    ) {
        val context = textMessageBinding.tvAITranslate.context
        if (message.isTranslated() == true) {
            textMessageBinding.tvMessage.text = message.getTranslation()
            textMessageBinding.tvAITranslate.text = context.getString(R.string.show_original)
        } else {
            textMessageBinding.tvMessage.text = message.getContent()
            textMessageBinding.tvAITranslate.text = context.getString(R.string.show_translate)
        }
    }

    private fun setAIListener(
        message: IncomingChatMessageEntity,
        listener: AIListener?,
        textMessageBinding: ForwardedReplyIncomingTextMessageBinding,
    ) {
        textMessageBinding.ivAI.setOnClickListener {
            val text = textMessageBinding.tvMessage.text.toString()
            message.setContent(text)
            listener?.onIconClick(message)
        }
    }

    private fun setTranslateListener(
        message: ForwardedRepliedMessageEntity,
        listener: AIListener?,
        textMessageBinding: ForwardedReplyIncomingTextMessageBinding,
    ) {
        textMessageBinding.tvAITranslate.setOnClickListener {
            listener?.onTranslateClick(message)

            if (message is AITranslateIncomingChatMessageEntity) {
                return@setOnClickListener
            }

            textMessageBinding.progressAI.visibility = View.VISIBLE

            if (QuickBloxUiKit.isEnabledAIAnswerAssistant()) {
                textMessageBinding.ivAI.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildTextOutgoingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
    ): View {
        val textMessageBinding = buildForwardedReplyOutgoingTextMessageBinding()

        val textMessage = message.getForwardedRepliedMessages()?.get(0)
        val sender = textMessage?.getSender()
        textMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        if (message.isForwarded()) {
            showForwardHeader(textMessageBinding.ivIcon, textMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(textMessageBinding.ivIcon, textMessageBinding.tvActionText)
            textMessageBinding.ivStatus.visibility = View.GONE
            textMessageBinding.tvTime.visibility = View.GONE
        }

        textMessageBinding.tvMessage.text = textMessage?.getContent()
        textMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        textMessageBinding.tvMessage.setTextColor(theme.getMainTextColor())
        textMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        textMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        textMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())
        textMessageBinding.tvMessage.backgroundTintList = ColorStateList.valueOf(theme.getOutgoingMessageColor())
        textMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        textMessageBinding.tvMessage.setTextColor(theme.getMainTextColor())

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        textMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        setState(message, theme, textMessageBinding.ivStatus)

        textMessageBinding.tvMessage.setOnLongClickListener {
            true
        }

        textMessageBinding.tvMessage.setOnTouchListener(
            TouchListener(textMessageBinding.tvMessage.context, textMessage, listener, textMessageBinding.tvMessage)
        )

        if (isForwardState) {
            textMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (textMessage?.getRelatedMessageId() != null && textMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    textMessageBinding.checkbox.isChecked = true
                }
            }
            textMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(textMessage)
                } else {
                    checkBoxListener?.onUnselected(textMessage)
                }
            }

            forwardedCheckBox = textMessageBinding.checkbox
        }

        return textMessageBinding.root
    }

    fun getCheckbox(): CheckBox? {
        return forwardedCheckBox
    }

    private fun buildMediaOutgoingMessage(
        message: ForwardedRepliedMessageEntity?,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val mediaContent = message?.getForwardedRepliedMessages()?.get(0)?.getMediaContent()
        return when (mediaContent?.getType()) {
            MediaContentEntity.Types.IMAGE -> {
                buildImageOutgoingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.AUDIO -> {
                buildAudioOutgoingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.VIDEO -> {
                buildVideoOutgoingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.FILE -> {
                buildFileOutgoingMessage(message, listener, theme, forwardState)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    private fun buildMediaIncomingMessage(
        message: ForwardedRepliedMessageEntity?,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val mediaContent = message?.getForwardedRepliedMessages()?.get(0)?.getMediaContent()
        return when (mediaContent?.getType()) {
            MediaContentEntity.Types.IMAGE -> {
                buildImageIncomingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.AUDIO -> {
                buildAudioIncomingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.VIDEO -> {
                buildVideoIncomingMessage(message, listener, theme, forwardState)
            }
            MediaContentEntity.Types.FILE -> {
                buildFileIncomingMessage(message, listener, theme, forwardState)
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildFileIncomingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val fileMessageBinding = buildForwardedReplyIncomingFileMessageBinding()

        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        if (message.isForwarded()) {
            showForwardHeader(fileMessageBinding.ivIcon, fileMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(fileMessageBinding.ivIcon, fileMessageBinding.tvActionText)
            fileMessageBinding.tvTime.visibility = View.GONE
        }

        fileMessageBinding.tvFileName.text = forwardedMessage?.getMediaContent()?.getName()
        fileMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        fileMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        fileMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        fileMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        fileMessageBinding.tvName.setTextColor(theme.getMainTextColor())
        fileMessageBinding.tvFileName.setTextColor(theme.getMainTextColor())

        val drawableBackground =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_forwarded_message) as GradientDrawable
        drawableBackground.setColor(theme.getIncomingMessageColor())
        fileMessageBinding.llFile.background = drawableBackground

        val drawablePlaceholder =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        drawablePlaceholder.setColor(theme.getTertiaryElementsColor())
        fileMessageBinding.ivFile.background = drawablePlaceholder

        val avatarHolder = ContextCompat.getDrawable(fileMessageBinding.root.context, R.drawable.user_avatar_holder)
        fileMessageBinding.ivAvatar.setImageDrawable(avatarHolder)

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(fileMessageBinding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        fileMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        val sender = forwardedMessage?.getSender()
        fileMessageBinding.tvName.text = sender?.getName()

        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            fileMessageBinding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        fileMessageBinding.clMessage.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        fileMessageBinding.clMessage.setOnLongClickListener {
            true
        }

        fileMessageBinding.clMessage.setOnTouchListener(
            TouchListener(
                fileMessageBinding.clMessage.context, forwardedMessage, listener, fileMessageBinding.clMessage
            )
        )

        if (forwardState) {
            fileMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    fileMessageBinding.checkbox.isChecked = true
                }
            }

            fileMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = fileMessageBinding.checkbox
        }


        return fileMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildVideoIncomingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val videoMessageBinding = buildForwardedReplyIncomingVideoMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        if (message.isForwarded()) {
            showForwardHeader(videoMessageBinding.ivIcon, videoMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(videoMessageBinding.ivIcon, videoMessageBinding.tvActionText)
            videoMessageBinding.tvTime.visibility = View.GONE
        }

        videoMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        videoMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        videoMessageBinding.tvName.setTextColor(theme.getMainTextColor())
        videoMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(videoMessageBinding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        videoMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        val avatarHolder = ContextCompat.getDrawable(videoMessageBinding.root.context, R.drawable.user_avatar_holder)
        videoMessageBinding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = forwardedMessage?.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            videoMessageBinding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        videoMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        applyIncomingVideoPlaceHolder(videoMessageBinding.ivVideo, theme)

        loadImageBy(
            forwardedMessage?.getMediaContent()?.getUrl(),
            videoMessageBinding.ivPlayButton,
            videoMessageBinding.progressBar,
            videoMessageBinding.ivVideo
        )

        videoMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        videoMessageBinding.ivVideo.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        videoMessageBinding.ivVideo.setOnLongClickListener {
            true
        }

        videoMessageBinding.ivVideo.setOnTouchListener(
            TouchListener(videoMessageBinding.ivVideo.context, forwardedMessage, listener, videoMessageBinding.ivVideo)
        )

        if (forwardState) {
            videoMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    videoMessageBinding.checkbox.isChecked = true
                }
            }

            videoMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = videoMessageBinding.checkbox
        }

        return videoMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildAudioIncomingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val audioMessageBinding = buildForwardedReplyIncomingAudioMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        if (message.isForwarded()) {
            showForwardHeader(audioMessageBinding.ivIcon, audioMessageBinding.tvActionText)
            audioMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()
        }

        if (message.isReplied()) {
            showReplyHeader(audioMessageBinding.ivIcon, audioMessageBinding.tvActionText)
            audioMessageBinding.tvTime.visibility = View.GONE
        }

        audioMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        audioMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        audioMessageBinding.tvName.setTextColor(theme.getMainTextColor())
        audioMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        audioMessageBinding.ivPlay.setColorFilter(theme.getMainElementsColor())

        val drawable = ContextCompat.getDrawable(
            audioMessageBinding.root.context, R.drawable.bg_forwarded_message
        ) as GradientDrawable
        drawable.setColor(theme.getIncomingMessageColor())
        audioMessageBinding.llFile.background = drawable

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(audioMessageBinding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        audioMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        audioMessageBinding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = forwardedMessage?.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            audioMessageBinding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        audioMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        audioMessageBinding.llFile.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        audioMessageBinding.llFile.setOnLongClickListener {
            true
        }

        audioMessageBinding.llFile.setOnTouchListener(
            TouchListener(audioMessageBinding.llFile.context, forwardedMessage, listener, audioMessageBinding.llFile)
        )

        if (forwardState) {
            audioMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    audioMessageBinding.checkbox.isChecked = true
                }
            }

            audioMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = audioMessageBinding.checkbox
        }

        return audioMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildImageIncomingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val imageMessageBinding = buildForwardedReplyIncomingImageMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        imageMessageBinding.llForward.visibility = View.VISIBLE
        if (message.isForwarded()) {
            showForwardHeader(imageMessageBinding.ivIcon, imageMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(imageMessageBinding.ivIcon, imageMessageBinding.tvActionText)
            imageMessageBinding.tvTime.visibility = View.GONE
        }

        imageMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        imageMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        imageMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())

        imageMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        imageMessageBinding.ivAvatar.setImageDrawable(avatarHolder)

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        imageMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        imageMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        val sender = forwardedMessage?.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            imageMessageBinding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }
        imageMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        applyIncomingImagePlaceHolder(forwardedMessage?.getMediaContent()?.isGif(), imageMessageBinding.ivImage, theme)

        loadImageFrom(forwardedMessage, imageMessageBinding.ivImage, imageMessageBinding.progressBar)

        imageMessageBinding.ivImage.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        imageMessageBinding.ivImage.setOnLongClickListener {
            true
        }

        imageMessageBinding.ivImage.setOnTouchListener(
            TouchListener(imageMessageBinding.ivImage.context, forwardedMessage, listener, imageMessageBinding.ivImage)
        )

        if (forwardState) {
            imageMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    imageMessageBinding.checkbox.isChecked = true
                }
            }

            imageMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = imageMessageBinding.checkbox
        }

        return imageMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildVideoOutgoingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val videoMessageBinding = buildForwardedReplyOutgoingVideoMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        val sender = forwardedMessage?.getSender()
        videoMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()
        videoMessageBinding.llForward.visibility = View.VISIBLE

        if (message.isForwarded()) {
            showForwardHeader(videoMessageBinding.ivIcon, videoMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(videoMessageBinding.ivIcon, videoMessageBinding.tvActionText)
            videoMessageBinding.ivStatus.visibility = View.GONE
            videoMessageBinding.tvTime.visibility = View.GONE
        }

        videoMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        videoMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        videoMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())

        videoMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        videoMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        videoMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        setState(message, theme, videoMessageBinding.ivStatus)

        applyOutgoingVideoPlaceHolder(videoMessageBinding.ivVideo, theme)

        loadImageBy(
            forwardedMessage?.getMediaContent()?.getUrl(),
            videoMessageBinding.ivPlayButton,
            videoMessageBinding.progressBar,
            videoMessageBinding.ivVideo
        )

        videoMessageBinding.ivVideo.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        videoMessageBinding.ivVideo.setOnLongClickListener {
            true
        }

        videoMessageBinding.ivVideo.setOnTouchListener(
            TouchListener(videoMessageBinding.ivVideo.context, forwardedMessage, listener, videoMessageBinding.ivVideo)
        )

        if (forwardState) {
            videoMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    videoMessageBinding.checkbox.isChecked = true
                }
            }

            videoMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = videoMessageBinding.checkbox
        }

        return videoMessageBinding.root
    }

    private fun loadImageBy(
        url: String?, ivPlayButton: AppCompatImageView,
        progressBar: ProgressBar, ivVideo: AppCompatImageView,
    ) {
        val context = binding.root.context

        val placeHolder: Drawable?
        val isDrawableNotExist = ivVideo.drawable == null
        if (isDrawableNotExist) {
            placeHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_video_placeholder)
        } else {
            placeHolder = ivVideo.drawable
        }

        val requestOptions = RequestOptions().frame(0)

        Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).placeholder(placeHolder)
            .listener(RequestVideoListenerImpl(ivPlayButton, progressBar)).into(ivVideo)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildFileOutgoingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val fileMessageBinding = buildForwardedReplyOutgoingFileMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)

        val sender = forwardedMessage?.getSender()
        fileMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()
        fileMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()
        fileMessageBinding.tvFileName.text = forwardedMessage?.getMediaContent()?.getName()

        if (message.isForwarded()) {
            showForwardHeader(fileMessageBinding.ivIcon, fileMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(fileMessageBinding.ivIcon, fileMessageBinding.tvActionText)
            fileMessageBinding.ivStatus.visibility = View.GONE
            fileMessageBinding.tvTime.visibility = View.GONE
        }

        fileMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        fileMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        fileMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())
        setState(message, theme, fileMessageBinding.ivStatus)

        val drawableBackground =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_forwarded_message) as GradientDrawable
        drawableBackground.setColor(theme.getOutgoingMessageColor())
        fileMessageBinding.llFile.background = drawableBackground

        val drawablePlaceholder =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        drawablePlaceholder.setColor(theme.getTertiaryElementsColor())
        fileMessageBinding.ivFile.background = drawablePlaceholder

        fileMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        fileMessageBinding.tvFileName.setTextColor(theme.getMainTextColor())

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        fileMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        fileMessageBinding.llFile.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        fileMessageBinding.llFile.setOnLongClickListener {
            true
        }

        fileMessageBinding.llFile.setOnTouchListener(
            TouchListener(fileMessageBinding.llFile.context, forwardedMessage, listener, fileMessageBinding.llFile)
        )

        if (forwardState) {
            fileMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    fileMessageBinding.checkbox.isChecked = true
                }
            }

            fileMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = fileMessageBinding.checkbox
        }

        return fileMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildAudioOutgoingMessage(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        forwardState: Boolean,
    ): View {
        val audioMessageBinding = buildForwardedReplyOutgoingAudioMessageBinding()
        val forwardedMessage = message.getForwardedRepliedMessages()?.get(0)
        val sender = forwardedMessage?.getSender()

        audioMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        if (message.isForwarded()) {
            showForwardHeader(audioMessageBinding.ivIcon, audioMessageBinding.tvActionText)
        }

        if (message.isReplied()) {
            showReplyHeader(audioMessageBinding.ivIcon, audioMessageBinding.tvActionText)
            audioMessageBinding.ivStatus.visibility = View.GONE
            audioMessageBinding.tvTime.visibility = View.GONE
        }

        audioMessageBinding.tvTime.text = message.getTime()?.convertToStringTime()

        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_forwarded_message) as GradientDrawable
        drawable.setColor(theme.getOutgoingMessageColor())
        audioMessageBinding.llFile.background = drawable

        audioMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())
        audioMessageBinding.ivPlay.setColorFilter(theme.getMainElementsColor())
        audioMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        audioMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        audioMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        audioMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        setState(message, theme, audioMessageBinding.ivStatus)

        audioMessageBinding.llFile.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        audioMessageBinding.llFile.setOnLongClickListener {
            true
        }

        audioMessageBinding.llFile.setOnTouchListener(
            TouchListener(audioMessageBinding.llFile.context, forwardedMessage, listener, audioMessageBinding.llFile)
        )

        if (forwardState) {
            audioMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    audioMessageBinding.checkbox.isChecked = true
                }
            }

            audioMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = audioMessageBinding.checkbox
        }
        return audioMessageBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildImageOutgoingMessage(
        message: ForwardedRepliedMessageEntity?,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
    ): View {
        val imageMessageBinding = buildForwardedReplyOutgoingImageMessageBinding()
        val forwardedMessage = message?.getForwardedRepliedMessages()?.get(0)

        val sender = forwardedMessage?.getSender()
        imageMessageBinding.tvName.text = sender?.getName() ?: sender?.getLogin()

        if (message?.isForwarded() == true) {
            showForwardHeader(imageMessageBinding.ivIcon, imageMessageBinding.tvActionText)
        }

        if (message?.isReplied() == true) {
            showReplyHeader(imageMessageBinding.ivIcon, imageMessageBinding.tvActionText)
            imageMessageBinding.ivStatus.visibility = View.GONE
            imageMessageBinding.tvTime.visibility = View.GONE
        }

        imageMessageBinding.ivIcon.setColorFilter(theme.getSecondaryTextColor())
        imageMessageBinding.tvActionText.setTextColor(theme.getSecondaryTextColor())
        imageMessageBinding.tvName.setTextColor(theme.getSecondaryTextColor())
        imageMessageBinding.tvTime.setTextColor(theme.getTertiaryElementsColor())

        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(theme.getMainElementsColor(), defaultColor)
        imageMessageBinding.checkbox.buttonTintList = ColorStateList(states, colors)

        imageMessageBinding.tvTime.text = message?.getTime()?.convertToStringTime()

        setState(message, theme, imageMessageBinding.ivStatus)

        imageMessageBinding.ivImage.setOnClickListener {
            listener?.onClick(forwardedMessage)
        }

        imageMessageBinding.ivImage.setOnLongClickListener {
            true
        }
        applyOutgoingImagePlaceHolder(forwardedMessage?.getMediaContent()?.isGif(), imageMessageBinding.ivImage, theme)

        imageMessageBinding.ivImage.setOnTouchListener(
            TouchListener(imageMessageBinding.ivImage.context, forwardedMessage, listener, imageMessageBinding.ivImage)
        )

        loadImageFrom(forwardedMessage, imageMessageBinding.ivImage, imageMessageBinding.progressBar)

        if (isForwardState) {
            imageMessageBinding.checkbox.visibility = View.VISIBLE

            if (selectedMessages?.isNotEmpty() == true && selectedMessages?.get(0) != null) {
                val foundMessage = selectedMessages?.get(0) as ForwardedRepliedMessageEntity
                if (forwardedMessage?.getRelatedMessageId() != null && forwardedMessage.getRelatedMessageId() == foundMessage.getRelatedMessageId()) {
                    imageMessageBinding.checkbox.isChecked = true
                }
            }

            imageMessageBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(forwardedMessage)
                } else {
                    checkBoxListener?.onUnselected(forwardedMessage)
                }
            }
            forwardedCheckBox = imageMessageBinding.checkbox
        }

        return imageMessageBinding.root
    }

    private fun buildForwardedReplyOutgoingTextMessageBinding(): ForwardedReplyOutgoingTextMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyOutgoingTextMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyOutgoingAudioMessageBinding(): ForwardedReplyOutgoingAudioMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyOutgoingAudioMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyOutgoingVideoMessageBinding(): ForwardedReplyOutgoingVideoMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyOutgoingVideoMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyOutgoingImageMessageBinding(): ForwardedReplyOutgoingImageMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyOutgoingImageMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyOutgoingFileMessageBinding(): ForwardedReplyOutgoingFileMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyOutgoingFileMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyIncomingTextMessageBinding(): ForwardedReplyIncomingTextMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyIncomingTextMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyIncomingAudioMessageBinding(): ForwardedReplyIncomingAudioMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyIncomingAudioMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyIncomingVideoMessageBinding(): ForwardedReplyIncomingVideoMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyIncomingVideoMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyIncomingImageMessageBinding(): ForwardedReplyIncomingImageMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyIncomingImageMessageBinding.inflate(inflater)
    }

    private fun buildForwardedReplyIncomingFileMessageBinding(): ForwardedReplyIncomingFileMessageBinding {
        val inflater = LayoutInflater.from(binding.root.context)
        return ForwardedReplyIncomingFileMessageBinding.inflate(inflater)
    }

    inner class TouchListener(
        context: Context,
        private val message: ForwardedRepliedMessageEntity?,
        private val listener: MessageListener?,
        private val view: View,
    ) : View.OnTouchListener {
        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, GestureListener())
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val x = e.rawX.toInt()
                val y = e.rawY.toInt()
                listener?.onLongClick(message, adapterPosition, view, x, y)
            }
        }
    }

    interface MessageListener {
        fun onClick(message: ChatMessageEntity?)
        fun onLongClick(
            message: ForwardedRepliedMessageEntity?, position: Int? = null, view: View,
            xRawTouch: Int, yRawTouch: Int,
        )
    }

    private fun loadImageFrom(
        message: ForwardedRepliedMessageEntity?,
        imageView: ImageView,
        progressView: View,
    ) {
        val context = binding.root.context
        val url = message?.getMediaContent()?.getUrl()
        val holderId: Int
        if (message?.getMediaContent()?.isGif() == true) {
            holderId = R.drawable.ic_gif_placeholder
        } else {
            holderId = R.drawable.ic_image_placeholder
        }

        Glide.with(context).load(url).placeholder(ContextCompat.getDrawable(binding.root.context, holderId))
            .listener(RequestAudioListenerImpl(progressView)).into(imageView)
    }

    private inner class RequestAudioListenerImpl(
        private val progressView: View,
    ) : RequestListener<Drawable> {
        init {
            progressView.visibility = View.VISIBLE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            progressView.visibility = View.GONE
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            progressView.visibility = View.GONE
            return false
        }
    }

    private inner class RequestVideoListenerImpl(
        private val ivPlayButton: AppCompatImageView,
        private val progressBar: ProgressBar,
    ) : RequestListener<Drawable> {
        init {
            progressBar.visibility = View.VISIBLE
            ivPlayButton.visibility = View.GONE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            progressBar.visibility = View.GONE
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            progressBar.visibility = View.GONE
            ivPlayButton.visibility = View.VISIBLE

            setPlayButtonBackground(ivPlayButton)
            return false
        }
    }

    private fun setPlayButtonBackground(ivPlayButton: AppCompatImageView) {
        val playButtonDrawable =
            ContextCompat.getDrawable(this.binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        playButtonDrawable.setColor(Color.parseColor("#99131D28"))
        ivPlayButton.background = playButtonDrawable
    }


    private fun applyOutgoingImagePlaceHolder(isGif: Boolean?, imageView: ImageView, theme: UiKitTheme) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getOutgoingMessageColor())
        imageView.background = drawable

        if (isGif == true) {
            imageView.setImageResource(R.drawable.ic_gif_placeholder)
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
        }

        imageView.scaleType = ImageView.ScaleType.CENTER
    }

    private fun applyIncomingImagePlaceHolder(isGif: Boolean?, imageView: ImageView, theme: UiKitTheme) {
        imageView.background = ContextCompat.getDrawable(binding.root.context, R.drawable.incoming_media_placeholder)
        imageView.backgroundTintList = ColorStateList.valueOf(theme.getIncomingMessageColor())

        if (isGif == true) {
            imageView.setImageResource(R.drawable.ic_gif_placeholder)
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
        }
        imageView.scaleType = ImageView.ScaleType.CENTER
    }

    private fun applyOutgoingVideoPlaceHolder(videoView: ImageView, theme: UiKitTheme) {
        val itemBackgroundDrawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        itemBackgroundDrawable.setColor(theme.getOutgoingMessageColor())
        videoView.background = itemBackgroundDrawable

        videoView.setImageResource(R.drawable.ic_video_placeholder)

        videoView.scaleType = ImageView.ScaleType.CENTER
    }

    private fun applyIncomingVideoPlaceHolder(videoView: AppCompatImageView, theme: UiKitTheme) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.incoming_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getIncomingMessageColor())
        videoView.background = drawable

        videoView.setImageResource(R.drawable.ic_video_placeholder)

        videoView.scaleType = ImageView.ScaleType.CENTER
    }

    protected fun setState(message: ForwardedRepliedMessageEntity?, theme: UiKitTheme, ivStatus: AppCompatImageView) {
        if (message is OutgoingChatMessageEntity) {
            val resourceId: Int?
            val color: Int?
            when (message.getOutgoingState()) {
                OutgoingChatMessageEntity.OutgoingStates.SENDING -> {
                    resourceId = R.drawable.sending
                    color = theme.getTertiaryElementsColor()
                }
                OutgoingChatMessageEntity.OutgoingStates.SENT -> {
                    resourceId = R.drawable.sent
                    color = theme.getTertiaryElementsColor()
                }
                OutgoingChatMessageEntity.OutgoingStates.DELIVERED -> {
                    resourceId = R.drawable.delivered
                    color = theme.getTertiaryElementsColor()
                }
                OutgoingChatMessageEntity.OutgoingStates.READ -> {
                    resourceId = R.drawable.read
                    color = theme.getMainElementsColor()
                }
                OutgoingChatMessageEntity.OutgoingStates.ERROR -> {
                    resourceId = R.drawable.send_error
                    color = theme.getErrorColor()
                }
                else -> {
                    return
                }
            }
            ivStatus.setImageResource(resourceId)
            ivStatus.setColorFilter(color)
        }
    }

    interface AIListener {
        fun onIconClick(message: ForwardedRepliedMessageEntity?)
        fun onTranslateClick(message: ForwardedRepliedMessageEntity?)
    }
}