/*
 * Created by Injoit on 12.5.2023.
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
import com.quickblox.android_ui_kit.databinding.FileOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class FileOutgoingViewHolder(binding: FileOutgiongMessageItemBinding) :
    BaseViewHolder<FileOutgiongMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): FileOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return FileOutgoingViewHolder(FileOutgiongMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: OutgoingChatMessageEntity?, listener: FileOutgoingListener?) {
        binding.tvTime.text = message?.getTime()?.convertToStringTime()
        binding.tvFileName.text = message?.getMediaContent()?.getName()

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    private fun setListener(message: OutgoingChatMessageEntity?, listener: FileOutgoingListener?) {
        binding.llFile.setOnClickListener {
            listener?.onFileClick(message)
        }

        binding.llFile.setOnLongClickListener {
            listener?.onFileLongClick(message)
            true
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundColor(theme.getOutgoingMessageColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        setFileNameColor(theme.getMainTextColor())
        setFilePlaceholderColor(theme.getTertiaryElementsColor())
    }

    private fun setFilePlaceholderColor(@ColorInt color: Int) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        drawable.setColor(color)
        binding.ivFile.background = drawable
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

    fun setFileNameColor(@ColorInt color: Int) {
        binding.tvFileName.setTextColor(color)
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

    interface FileOutgoingListener {
        fun onFileClick(message: OutgoingChatMessageEntity?)
        fun onFileLongClick(message: OutgoingChatMessageEntity?)
    }
}