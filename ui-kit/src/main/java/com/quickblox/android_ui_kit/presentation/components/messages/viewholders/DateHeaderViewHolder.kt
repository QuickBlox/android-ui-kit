/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.quickblox.android_ui_kit.databinding.DateHeaderMessageItemBinding
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.DateHeaderMessageEntity
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class DateHeaderViewHolder(binding: DateHeaderMessageItemBinding) :
    BaseViewHolder<DateHeaderMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): DateHeaderViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return DateHeaderViewHolder(DateHeaderMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: DateHeaderMessageEntity?) {
        binding.tvDate.text = message?.getText()
        applyTheme(theme)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundHeader(theme.getInputBackgroundColor())
        setTextHeaderColor(theme.getSecondaryElementsColor())
    }

    fun setBackgroundHeader(@ColorInt color: Int) {
        binding.tvDate.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun setTextHeaderColor(@ColorInt color: Int) {
        binding.tvDate.setTextColor(color)
    }
}