/*
 * Created by Injoit on 19.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.users.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.AddMembersItemBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class AddMembersViewHolder(binding: AddMembersItemBinding) : BaseViewHolder<AddMembersItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true

    companion object {
        fun newInstance(parent: ViewGroup): AddMembersViewHolder {
            return AddMembersViewHolder(
                AddMembersItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(user: UserEntity?, addClickListener: ((result: UserEntity?) -> Unit)? = null) {
        binding.tvName.text = user?.getName()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        if (isVisibleAvatar) {
            binding.ivAvatar.loadCircleImageFromUrl(user?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        binding.btnAdd.setOnClickListener {
            addClickListener?.invoke(user)
        }

        applyTheme(theme)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())
        setNameColor(theme.getMainTextColor())
        setBackground(theme.getMainBackgroundColor())
        binding.btnAdd.setColorFilter(theme.getMainElementsColor())
        binding.btnAdd.makeClickableBackground(theme.getMainElementsColor())
    }

    fun setBackground(@ColorInt color: Int) {
        binding.root.setBackgroundColor(color)
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.vDivider.setBackgroundColor(color)
    }
}