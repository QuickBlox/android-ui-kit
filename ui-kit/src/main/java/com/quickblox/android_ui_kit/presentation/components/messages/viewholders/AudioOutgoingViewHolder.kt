/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.AudioOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AudioOutgoingViewHolder(binding: AudioOutgiongMessageItemBinding) :
    BaseViewHolder<AudioOutgiongMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): AudioOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return AudioOutgoingViewHolder(AudioOutgiongMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: OutgoingChatMessageEntity?, listener: AudioOutgoingListener?) {
        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    private fun setListener(message: OutgoingChatMessageEntity?, listener: AudioOutgoingListener?) {
        binding.llFile.setOnClickListener {
            listener?.onAudioClick(message)
        }

        binding.llFile.setOnLongClickListener {
            listener?.onAudioLongClick(message)
            true
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundColor(theme.getOutgoingMessageColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        setPlayImageColor(theme.getMainElementsColor())
    }

    private fun setPlayImageColor(@ColorInt color: Int) {
        binding.ivPlay.setColorFilter(color)
    }

    private fun setBackgroundColor(@ColorInt color: Int) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_outgoing_message) as GradientDrawable
        drawable.setColor(color)
        binding.llFile.background = drawable
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun setState(message: OutgoingChatMessageEntity?) {
        val resourceId: Int?
        val color: Int?
        when (message?.getOutgoingState()) {
            OutgoingChatMessageEntity.OutgoingStates.SENDING -> {
                resourceId = R.drawable.sending
                color = theme.getTertiaryElementsColor()

                binding.ivStatus.setImageResource(resourceId)
                binding.ivStatus.setColorFilter(color)
            }
            OutgoingChatMessageEntity.OutgoingStates.SENT -> {
                resourceId = R.drawable.sent
                color = theme.getTertiaryElementsColor()

                binding.ivStatus.setImageResource(resourceId)
                binding.ivStatus.setColorFilter(color)
            }
            OutgoingChatMessageEntity.OutgoingStates.DELIVERED -> {
                resourceId = R.drawable.delivered
                color = theme.getTertiaryElementsColor()

                binding.ivStatus.setImageResource(resourceId)
                binding.ivStatus.setColorFilter(color)
            }
            OutgoingChatMessageEntity.OutgoingStates.READ -> {
                resourceId = R.drawable.read
                color = theme.getMainElementsColor()

                binding.ivStatus.setImageResource(resourceId)
                binding.ivStatus.setColorFilter(color)
            }
            OutgoingChatMessageEntity.OutgoingStates.ERROR -> {
                resourceId = R.drawable.send_error
                color = theme.getErrorColor()

                binding.ivStatus.setImageResource(resourceId)
                binding.ivStatus.setColorFilter(color)
            }
            else -> {}
        }
    }

    interface AudioOutgoingListener {
        fun onAudioClick(message: OutgoingChatMessageEntity?)
        fun onAudioLongClick(message: OutgoingChatMessageEntity?)
    }
}