/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.*
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.AudioIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.MessageListener
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AudioIncomingViewHolder(binding: AudioIncomingMessageItemBinding) :
    BaseViewHolder<AudioIncomingMessageItemBinding>(binding), Forward {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null

    companion object {
        fun newInstance(parent: ViewGroup): AudioIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return AudioIncomingViewHolder(AudioIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(
        message: IncomingChatMessageEntity?,
        listener: MessageListener?,
        isForwardState: Boolean,
        selectedMessages: MutableList<MessageEntity>,
    ) {
        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = message?.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            binding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        setListener(message, listener)

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

        applyTheme(theme)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener(message: IncomingChatMessageEntity?, listener: MessageListener?) {
        binding.llFile.setOnClickListener {
            listener?.onClick(message)
        }
        binding.llFile.setOnLongClickListener {
            true
        }
        binding.llFile.setOnTouchListener(
            TouchListener(binding.llFile.context, message, listener, binding.llFile)
        )
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundColor(theme.getIncomingMessageColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        setNameColor(theme.getMainTextColor())
        setPlayImageColor(theme.getMainElementsColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    private fun setPlayImageColor(@ColorInt color: Int) {
        binding.ivPlay.setColorFilter(color)
    }

    private fun setBackgroundColor(@ColorInt color: Int) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_ingoing_message) as GradientDrawable
        drawable.setColor(color)
        binding.llFile.background = drawable
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    override fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>) {
        binding.checkbox.isChecked = checked
    }

    fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
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