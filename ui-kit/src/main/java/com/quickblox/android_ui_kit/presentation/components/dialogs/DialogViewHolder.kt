/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogGroupItemBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.PRIVATE
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.screens.modifyDialogsDateStringFrom
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogViewHolder(binding: DialogGroupItemBinding) : BaseViewHolder<DialogGroupItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true
    private var isVisibleCounter: Boolean = true

    companion object {
        fun newInstance(parent: ViewGroup): DialogViewHolder {
            return DialogViewHolder(
                DialogGroupItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun bind(dialog: DialogEntity) {
        binding.tvDialogName.text = dialog.getName()
        binding.tvLastMessage.text = buildLastMessageFrom(dialog.getLastMessage() as IncomingChatMessageEntity)

        dialog.getLastMessage()?.getTime()?.let { time ->
            val modifiedTime = modifyDialogsDateStringFrom(time)
            binding.tvTime.text = modifiedTime
        }

        if (dialog.getType() == PRIVATE) {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.private_holder)
        } else {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.group_holder)
        }

        applyTheme(theme)
    }

    private fun buildLastMessageFrom(message: IncomingChatMessageEntity): String {
        if (message.isMediaContent()) {
            return "Attachment ${message.getMediaContent()?.getName()}"
        } else {
            return message.getContent() ?: ""
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())
        setCounterTextColor(theme.getMainBackgroundColor())
        setCounterBackgroundColor(theme.getMainElementsColor())
        setTimeColor(theme.getSecondaryTextColor())
        setLastMessageColor(theme.getSecondaryTextColor())
        setDialogNameColor(theme.getSecondaryElementsColor())
        binding.root.makeClickableBackground(theme.getMainElementsColor())
    }

    fun setDialogNameColor(@ColorInt color: Int) {
        binding.tvDialogName.setTextColor(color)
    }

    fun setLastMessageColor(@ColorInt color: Int) {
        binding.tvLastMessage.setTextColor(color)
    }

    fun setTimeColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    @SuppressLint("RestrictedApi")
    fun setCounterBackgroundColor(@ColorInt color: Int) {
        binding.tvCounter.supportBackgroundTintList = ColorStateList.valueOf(color)
    }

    fun setCounterTextColor(@ColorInt color: Int) {
        binding.tvCounter.setTextColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.vDivider.setBackgroundColor(color)
    }

    fun setVisibleAvatar(visible: Boolean) {
        isVisibleAvatar = visible
        binding.ivAvatar.setVisibility(visible)
    }

    fun setVisibleLastMessage(visible: Boolean) {
        binding.tvLastMessage.setVisibility(visible)
    }

    fun setVisibleTime(visible: Boolean) {
        binding.tvTime.setVisibility(visible)
    }

    fun setVisibleCounter(visible: Boolean) {
        isVisibleCounter = visible
        binding.tvCounter.setVisibility(visible)
    }
}
