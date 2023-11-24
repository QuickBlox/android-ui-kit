/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.*
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.TextOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity.OutgoingStates.*
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class TextOutgoingViewHolder(binding: TextOutgiongMessageItemBinding) :
    BaseMessageViewHolder<TextOutgiongMessageItemBinding>(binding), Forward {
    private var message: ForwardedRepliedMessageEntity? = null

    private var theme: UiKitTheme = LightUIKitTheme()
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null

    override fun clearCachedData() {
        binding.flForwardReplyContainer.removeAllViews()
    }

    companion object {
        fun newInstance(parent: ViewGroup): TextOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TextOutgoingViewHolder(TextOutgiongMessageItemBinding.inflate(inflater, parent, false))
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
                binding.clMessage.visibility = View.GONE
                binding.checkbox.visibility = View.GONE
                applyTheme(theme)
                return
            }
        }
        binding.clMessage.visibility = View.VISIBLE

        binding.tvMessage.text = message?.getContent()
        binding.tvMessage.setTextColor(theme.getMainTextColor())

        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        setListener(message, listener)
        setState(message)

        if (isForwardState) {
            binding.checkbox.isChecked = false
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
                val isChecked = !binding.checkbox.isChecked
                if (isChecked) {
                    binding.checkbox.isChecked = true
                    checkBoxListener?.onSelected(message)
                } else {
                    binding.checkbox.isChecked = false
                    checkBoxListener?.onUnselected(message)
                }
            }
        }

        applyTheme(theme)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener(message: OutgoingChatMessageEntity?, listener: MessageListener?) {
        binding.tvMessage.setOnClickListener {
            listener?.onClick(message)
        }
        binding.tvMessage.setOnLongClickListener {
            true
        }
        binding.tvMessage.setOnTouchListener(
            TouchListener(binding.tvMessage.context, message, listener, binding.tvMessage)
        )
    }

    private fun setState(message: OutgoingChatMessageEntity?) {
        val resourceId: Int?
        val color: Int?
        when (message?.getOutgoingState()) {
            SENDING -> {
                resourceId = R.drawable.sending
                color = theme.getTertiaryElementsColor()
            }
            SENT -> {
                resourceId = R.drawable.sent
                color = theme.getTertiaryElementsColor()
            }
            DELIVERED -> {
                resourceId = R.drawable.delivered
                color = theme.getTertiaryElementsColor()
            }
            READ -> {
                resourceId = R.drawable.read
                color = theme.getMainElementsColor()
            }
            ERROR -> {
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

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundMessageColor(theme.getOutgoingMessageColor())
        setTimeTextColor(theme.getTertiaryElementsColor())

        binding.tvMessage.setTextColor(theme.getMainTextColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
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
    }

    fun setBackgroundMessageColor(@ColorInt color: Int) {
        binding.tvMessage.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    override fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        super.setCheckBoxListener(checkBoxListener)
        this.checkBoxListener = checkBoxListener
    }

    private fun showForwardedReplyMessages(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
    ) {
        if (message.getForwardedRepliedMessages()?.isEmpty() == true){
            return
        }
        val forwardReplyView = buildOutgoingMessage(message, listener, theme, isForwardState)
        binding.flForwardReplyContainer.addView(forwardReplyView)
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
                Log.d("myLogs", "onLongPress: SenderId ----  ${message?.getSenderId()}")
            }
        }
    }
}