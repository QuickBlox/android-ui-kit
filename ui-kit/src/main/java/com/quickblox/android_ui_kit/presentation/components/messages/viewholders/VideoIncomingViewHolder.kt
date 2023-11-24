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
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.VideoIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.MessageListener
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class VideoIncomingViewHolder(binding: VideoIncomingMessageItemBinding) :
    BaseViewHolder<VideoIncomingMessageItemBinding>(binding), Forward {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null

    companion object {
        fun newInstance(parent: ViewGroup): VideoIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return VideoIncomingViewHolder(VideoIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(
        message: IncomingChatMessageEntity,
        listener: MessageListener?,
        isForwardState: Boolean,
        selectedMessages: MutableList<MessageEntity>, ) {
        binding.tvTime.text = message.getTime()?.convertToStringTime()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = message.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            binding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        applyPlaceHolder()

        loadImageBy(message.getMediaContent()?.getUrl())

        binding.tvName.text = sender?.getName() ?: sender?.getLogin()

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

            binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(message)
                } else {
                    checkBoxListener?.onUnselected(message)
                }
            }
        }

        setListener(message, listener)

        applyTheme(theme)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener(message: IncomingChatMessageEntity?, listener: MessageListener?) {
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

        Glide.with(context).load(url)
            .placeholder(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_video_placeholder)).frame(0)
            .listener(RequestListenerImpl()).into(binding.ivVideo)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setNameColor(theme.getTertiaryElementsColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun applyPlaceHolder() {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.incoming_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getIncomingMessageColor())
        binding.ivVideo.background = drawable

        binding.ivVideo.setImageResource(R.drawable.ic_video_placeholder)

        binding.ivVideo.scaleType = ImageView.ScaleType.CENTER
    }

    fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
    }

    override fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>) {
        binding.checkbox.isChecked = checked
    }

    private inner class RequestListenerImpl : RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
            binding.ivPlayButton.visibility = View.GONE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder()
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
        private val message: IncomingChatMessageEntity?,
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