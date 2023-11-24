/*
 * Created by Injoit on 8.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogSelectionGroupItemBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogSelectionViewHolder(binding: DialogSelectionGroupItemBinding) :
    BaseViewHolder<DialogSelectionGroupItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true
    private var checkBoxListener: CheckBoxListener? = null

    companion object {
        fun newInstance(parent: ViewGroup): DialogSelectionViewHolder {
            return DialogSelectionViewHolder(
                DialogSelectionGroupItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun bind(dialog: DialogEntity) {
        applyTheme(theme)

        binding.tvDialogName.text = dialog.getName()
        if (dialog.getType() == DialogEntity.Types.PRIVATE) {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.private_holder)
        } else {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.group_holder)
        }

        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkBoxListener?.onSelected(dialog)
            } else {
                checkBoxListener?.onUnselected(dialog)
            }
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())

        setDialogNameColor(theme.getMainTextColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setChecked(checked: Boolean) {
        binding.checkbox.isChecked = checked
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    fun setDialogNameColor(@ColorInt color: Int) {
        binding.tvDialogName.setTextColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.vDivider.setBackgroundColor(color)
    }

    fun setVisibleAvatar(visible: Boolean) {
        isVisibleAvatar = visible
        binding.ivAvatar.setVisibility(visible)
    }

    fun setCheckBoxListener(checkBoxListener: CheckBoxListener) {
        this.checkBoxListener = checkBoxListener
    }

    interface CheckBoxListener {
        fun onSelected(dialog: DialogEntity)
        fun onUnselected(dialog: DialogEntity)
    }
}
