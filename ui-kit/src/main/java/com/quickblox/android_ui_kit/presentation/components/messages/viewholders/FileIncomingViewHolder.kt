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
import com.quickblox.android_ui_kit.databinding.FileIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class FileIncomingViewHolder(binding: FileIncomingMessageItemBinding) :
    BaseViewHolder<FileIncomingMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): FileIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return FileIncomingViewHolder(FileIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: IncomingChatMessageEntity?, listener: FileIncomingListener?) {
        binding.tvFileName.text = message?.getMediaContent()?.getName()

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

        applyTheme(theme)
    }

    private fun setListener(message: IncomingChatMessageEntity?, listener: FileIncomingListener?) {
        binding.llFile.setOnClickListener {
            listener?.onFileClick(message)
        }

        binding.llFile.setOnLongClickListener {
            listener?.onFileLongClick(message)
            true
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundColor(theme.getIncomingMessageColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        setNameColor(theme.getMainTextColor())
        setPlaceholderColor(theme.getTertiaryElementsColor())
        setFileNameColor(theme.getMainTextColor())
    }

    private fun setPlaceholderColor(@ColorInt color: Int) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        drawable.setColor(color)
        binding.ivFile.background = drawable
    }

    private fun setBackgroundColor(@ColorInt color: Int) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_ingoing_message) as GradientDrawable
        drawable.setColor(color)
        binding.llFile.background = drawable
    }

    fun setFileNameColor(@ColorInt color: Int) {
        binding.tvFileName.setTextColor(color)
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    interface FileIncomingListener {
        fun onFileClick(message: IncomingChatMessageEntity?)
        fun onFileLongClick(message: IncomingChatMessageEntity?)
    }
}