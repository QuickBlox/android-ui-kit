/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.quickblox.android_ui_kit.databinding.EventMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.EventMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class EventViewHolder(binding: EventMessageItemBinding) :
    BaseViewHolder<EventMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): EventViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return EventViewHolder(EventMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: EventMessageEntity?) {
        binding.tvMessage.text = message?.getText()
        applyTheme(theme)
    }

    private fun applyTheme(theme: UiKitTheme) {
        binding.tvMessage.setTextColor(theme.getTertiaryElementsColor())
    }
}