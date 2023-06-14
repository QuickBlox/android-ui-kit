/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.TextOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity.OutgoingStates.*
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class TextOutgoingViewHolder(binding: TextOutgiongMessageItemBinding) :
    BaseViewHolder<TextOutgiongMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

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

    fun bind(message: OutgoingChatMessageEntity?, listener: TextOutgoingListener?) {
        binding.tvMessage.text = message?.getContent()
        binding.tvMessage.setTextColor(theme.getMainTextColor())

        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    private fun setListener(message: OutgoingChatMessageEntity?, listener: TextOutgoingListener?) {
        binding.tvMessage.setOnClickListener {
            listener?.onTextClick(message)
        }

        binding.tvMessage.setOnLongClickListener {
            listener?.onTextLongClick(message)
            true
        }
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
    }

    fun setBackgroundMessageColor(@ColorInt color: Int) {
        binding.tvMessage.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    interface TextOutgoingListener {
        fun onTextClick(message: OutgoingChatMessageEntity?)
        fun onTextLongClick(message: OutgoingChatMessageEntity?)
    }
}