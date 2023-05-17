/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.users.members

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.MemberItemBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class MemberViewHolder(binding: MemberItemBinding) : BaseViewHolder<MemberItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true

    companion object {
        fun newInstance(parent: ViewGroup): MemberViewHolder {
            return MemberViewHolder(
                MemberItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(
        user: UserEntity?,
        loggedUserId: Int?,
        ownerId: Int?,
        removeClickListener: ((result: UserEntity?) -> Unit)? = null
    ) {
        var name = user?.getName() ?: user?.getLogin()

        val isLoggedUser = user?.getUserId() == loggedUserId
        if (isLoggedUser) {
            name = "$name (You)"
        }

        val isOwner = user?.getUserId() == ownerId
        if (isOwner) {
            binding.tvAdmin.setVisibility(true)
        }

        val isOwnerAndNotLoggedUser = loggedUserId == ownerId && !isLoggedUser
        if (isOwnerAndNotLoggedUser) {
            binding.btnRemove.setVisibility(true)
        }

        binding.tvName.text = name

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        if (isVisibleAvatar) {
            binding.ivAvatar.loadCircleImageFromUrl(user?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        binding.btnRemove.setOnClickListener {
            removeClickListener?.invoke(user)
        }

        applyTheme(theme)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())
        setNameColor(theme.getMainTextColor())
        setBackground(theme.getMainBackgroundColor())
        binding.btnRemove.setColorFilter(theme.getMainElementsColor())
        binding.tvAdmin.setTextColor(theme.getSecondaryTextColor())
        binding.tvAdmin.text = binding.root.context.getString(R.string.admin)
        binding.btnRemove.makeClickableBackground(theme.getMainElementsColor())
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