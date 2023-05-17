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
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.TextIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class TextIncomingViewHolder(binding: TextIncomingMessageItemBinding) :
    BaseViewHolder<TextIncomingMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): TextIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TextIncomingViewHolder(TextIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: IncomingChatMessageEntity?, listener: TextIncomingListener?) {
        binding.tvMessage.text = message?.getContent()
        binding.tvMessage.setTextColor(theme.getMainTextColor())

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

    private fun setListener(message: IncomingChatMessageEntity?, listener: TextIncomingListener?) {
        binding.tvMessage.setOnClickListener {
            listener?.onTextClick(message)
        }

        binding.tvMessage.setOnLongClickListener {
            listener?.onTextLongClick(message)
            true
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundMessageColor(theme.getIncomingMessageColor())
        setNameColor(theme.getTertiaryElementsColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
    }

    fun setBackgroundMessageColor(@ColorInt color: Int) {
        binding.tvMessage.setBackgroundTintList(ColorStateList.valueOf(color))
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    interface TextIncomingListener {
        fun onTextClick(message: IncomingChatMessageEntity?)
        fun onTextLongClick(message: IncomingChatMessageEntity?)
    }
}