/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView.ScaleType.CENTER
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ImageOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity.OutgoingStates
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.MessageListener
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter.*
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class ImageOutgoingViewHolder(binding: ImageOutgiongMessageItemBinding) :
    BaseViewHolder<ImageOutgiongMessageItemBinding>(binding), Forward {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var checkBoxListener: CheckBoxListener? = null
    private var forwardedCheckBox: CheckBox? = null

    private var message: ForwardedRepliedMessageEntity? = null

    companion object {
        fun newInstance(parent: ViewGroup): ImageOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ImageOutgoingViewHolder(ImageOutgiongMessageItemBinding.inflate(inflater, parent, false))
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
        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        applyPlaceHolder(message?.getMediaContent()?.isGif())

        // TODO: Need to refactor
        val isNotSendingState = message?.getOutgoingState() != OutgoingStates.SENDING
        if (isNotSendingState) {
            loadImageFrom(message)
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

            binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkBoxListener?.onSelected(message)
                } else {
                    checkBoxListener?.onUnselected(message)
                }
            }
        }

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener(message: OutgoingChatMessageEntity?, listener: MessageListener?) {
        binding.ivImage.setOnClickListener {
            listener?.onClick(message)
        }
        binding.ivImage.setOnLongClickListener {
            true
        }
        binding.ivImage.setOnTouchListener(
            TouchListener(binding.ivImage.context, message, listener, binding.ivImage)
        )
    }

    private fun loadImageFrom(message: OutgoingChatMessageEntity?) {
        val context = binding.root.context
        val url = message?.getMediaContent()?.getUrl()
        val holderId: Int
        if (message?.getMediaContent()?.isGif() == true) {
            holderId = R.drawable.ic_gif_placeholder
        } else {
            holderId = R.drawable.ic_image_placeholder
        }

        Glide.with(context).load(url).placeholder(ContextCompat.getDrawable(binding.root.context, holderId))
            .listener(RequestListenerImpl(message)).into(binding.ivImage)
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

    private fun applyPlaceHolder(isGif: Boolean?) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getOutgoingMessageColor())
        binding.ivImage.background = drawable

        if (isGif == true) {
            binding.ivImage.setImageResource(R.drawable.ic_gif_placeholder)
        } else {
            binding.ivImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        binding.ivImage.scaleType = CENTER
    }

    private fun setState(message: OutgoingChatMessageEntity?) {
        val resourceId: Int?
        val color: Int?
        when (message?.getOutgoingState()) {
            OutgoingStates.SENDING -> {
                resourceId = R.drawable.sending
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.SENT -> {
                resourceId = R.drawable.sent
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.DELIVERED -> {
                resourceId = R.drawable.delivered
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.READ -> {
                resourceId = R.drawable.read
                color = theme.getMainElementsColor()
            }
            OutgoingStates.ERROR -> {
                resourceId = R.drawable.send_error
                color = theme.getErrorColor()
            }
            else -> {
                return
            }
        }

        binding.ivStatus.setImageResource(resourceId)
        binding.ivStatus.setColorFilter(color)
    }

    override fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>) {
        if (message?.isForwardedOrReplied() == true) {
            if (selectedMessages.contains(message as MessageEntity)) {
                binding.checkbox.isChecked = checked
            } else {
                forwardedCheckBox?.isChecked = checked
            }
        } else {
            binding.checkbox.isChecked = checked
        }
    }

    private inner class RequestListenerImpl(private val message: OutgoingChatMessageEntity?) :
        RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder(message?.getMediaContent()?.isGif())
            binding.progressBar.visibility = View.GONE
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            binding.progressBar.visibility = View.GONE
            return false
        }
    }

     fun setCheckBoxListener(checkBoxListener: CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
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