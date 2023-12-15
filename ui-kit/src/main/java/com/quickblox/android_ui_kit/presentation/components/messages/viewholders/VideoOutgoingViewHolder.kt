/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.VideoOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class VideoOutgoingViewHolder(binding: VideoOutgiongMessageItemBinding) :
    BaseMessageViewHolder<VideoOutgiongMessageItemBinding>(binding), Forward {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null
    private var message: ForwardedRepliedMessageEntity? = null

    override fun clearCachedData() {
        binding.flForwardReplyContainer.removeAllViews()
    }

    companion object {
        fun newInstance(parent: ViewGroup): VideoOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return VideoOutgoingViewHolder(VideoOutgiongMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(
        message: OutgoingChatMessageEntity?,
        listener: MessageListener?,
        isForwardState: Boolean,
        selectedMessages: MutableList<MessageEntity>,
    ) {
        this.message = message
        if (message?.isForwardedOrReplied() == true) {
            setSelectedMessages(selectedMessages)
            showForwardedReplyMessages(
                message,
                listener,
                theme,
                isForwardState
            )

            val content = message.getContent()
            if (content.isNullOrEmpty() || content.contains("[Forwarded_Message]")) {
                binding.ivVideo.visibility = View.GONE
                binding.ivStatus.visibility = View.GONE
                binding.tvTime.visibility = View.GONE
                binding.checkbox.visibility = View.GONE
                applyTheme(theme)
                return
            }
        }

        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        applyPlaceHolder()

        // TODO: Need to refactor
        val isNotSendingState = message?.getOutgoingState() != OutgoingChatMessageEntity.OutgoingStates.SENDING
        if (isNotSendingState) {
            loadImageBy(message?.getMediaContent()?.getUrl())
        }

        if (isForwardState) {
            binding.checkbox.visibility = View.VISIBLE

            if (selectedMessages.isNotEmpty()) {
                val foundMessage = selectedMessages.get(0)

                if (foundMessage is ForwardedRepliedMessageEntity && message?.getMessageId() != null
                    && message.getMessageId() == foundMessage.getMessageId() && foundMessage.getRelatedMessageId()
                        .isNullOrEmpty()
                ) {
                    binding.checkbox.isChecked = true
                }
            }

            binding.checkbox.setOnClickListener {
                val isChecked = binding.checkbox.isChecked
                if (isChecked) {
                    binding.checkbox.isChecked = true
                    checkBoxListener?.onSelected(message)
                } else {
                    binding.checkbox.isChecked = false
                    checkBoxListener?.onUnselected(message)
                }
            }
        }

        setListener(message, listener)
        setState(message, theme, binding.ivStatus)

        applyTheme(theme)
    }

    private fun showForwardedReplyMessages(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
    ) {
        if (message.getForwardedRepliedMessages()?.isEmpty() == true) {
            return
        }
        val forwardReplyView = buildOutgoingMessage(message, listener, theme, isForwardState)
        binding.flForwardReplyContainer.addView(forwardReplyView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener(message: OutgoingChatMessageEntity?, listener: MessageListener?) {
        binding.ivVideo.setOnClickListener {
            listener?.onClick(message)
        }
        binding.ivVideo.setOnLongClickListener {
            true
        }
        binding.ivVideo.setOnTouchListener(
            TouchListener(binding.ivVideo.context, message, listener, binding.ivVideo)
        )
    }

    private fun loadImageBy(url: String?) {
        val context = binding.root.context

        val placeHolder: Drawable?
        val isDrawableNotExist = binding.ivVideo.drawable == null
        if (isDrawableNotExist) {
            placeHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_video_placeholder)
        } else {
            placeHolder = binding.ivVideo.drawable
        }

        val requestOptions = RequestOptions().frame(0)

        Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).placeholder(placeHolder)
            .listener(RequestListenerImpl()).into(binding.ivVideo)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setTimeTextColor(theme.getTertiaryElementsColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun applyPlaceHolder() {
        val itemBackgroundDrawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        itemBackgroundDrawable.setColor(theme.getOutgoingMessageColor())
        binding.ivVideo.background = itemBackgroundDrawable

        binding.ivVideo.setImageResource(R.drawable.ic_video_placeholder)

        binding.ivVideo.scaleType = ImageView.ScaleType.CENTER
    }

    override fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        super.setCheckBoxListener(checkBoxListener)
        this.checkBoxListener = checkBoxListener
    }

    override fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>) {
        if (message?.isForwardedOrReplied() == true) {
            if (selectedMessages.contains(message as MessageEntity)) {
                binding.checkbox.isChecked = checked
            } else {
                getCheckbox()?.isChecked = checked
            }
        } else {
            binding.checkbox.isChecked = checked
        }
        if (!checked) {
            checkBoxListener?.onUnselected(message)
        }
    }

    private inner class RequestListenerImpl : RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
            binding.ivPlayButton.visibility = View.GONE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            binding.progressBar.visibility = View.GONE
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            binding.progressBar.visibility = View.GONE
            binding.ivPlayButton.visibility = View.VISIBLE

            setPlayButtonBackground()
            return false
        }
    }

    private fun setPlayButtonBackground() {
        val playButtonDrawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        playButtonDrawable.setColor(Color.parseColor("#99131D28"))
        binding.ivPlayButton.background = playButtonDrawable
    }

    inner class TouchListener(
        context: Context,
        private val message: OutgoingChatMessageEntity?,
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
}