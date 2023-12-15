/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.users.selection

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.SelectionUserItemBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class SelectionUserViewHolder(binding: SelectionUserItemBinding) : BaseViewHolder<SelectionUserItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true
    private var checkBoxListener: CheckBoxListener? = null

    companion object {
        fun newInstance(parent: ViewGroup): SelectionUserViewHolder {
            return SelectionUserViewHolder(
                SelectionUserItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun bind(user: UserEntity?, isSelected: Boolean) {
        binding.tvName.text = user?.getName() ?: user?.getLogin()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        if (isVisibleAvatar) {
            binding.ivAvatar.loadCircleImageFromUrl(user?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkBoxListener?.onSelected(user)
            } else {
                checkBoxListener?.onUnselected(user)
            }
        }
        binding.checkbox.isChecked = isSelected

        applyTheme(theme)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())
        setNameColor(theme.getMainTextColor())
        setBackground(theme.getMainBackgroundColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setChecked(checked: Boolean) {
        binding.checkbox.isChecked = checked
    }

    fun setBackground(@ColorInt color: Int) {
        binding.root.setBackgroundColor(color)
    }

    fun setCheckBoxListener(checkBoxListener: CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.vDivider.setBackgroundColor(color)
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    interface CheckBoxListener {
        fun onSelected(user: UserEntity?)
        fun onUnselected(user: UserEntity?)
    }
}
