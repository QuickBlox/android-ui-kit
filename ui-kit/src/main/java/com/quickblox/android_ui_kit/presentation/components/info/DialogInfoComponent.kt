/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.info

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.quickblox.android_ui_kit.presentation.components.Component

interface DialogInfoComponent : Component {
    fun getAvatarView(): AppCompatImageView?
    fun loadAvatar(avatarUrl: String, drawable: Drawable?)
    fun setDefaultPlaceHolderAvatar()

    fun setTextName(text: String)
    fun setTextColorName(@ColorInt color: Int)

    fun getMembersItemClickListener(): (() -> Unit)?
    fun setMembersItemClickListener(listener: (() -> Unit)?)
    fun setVisibleMembersItem(visible: Boolean)
    fun setCountMembers(members: Int)

    fun getLeaveItemClickListener(): (() -> Unit)?
    fun setLeaveItemClickListener(listener: (() -> Unit)?)
    fun setVisibleLeaveItem(visible: Boolean)

    fun setDividerColor(@ColorInt color: Int)
    fun setBackground(@ColorInt color: Int)

    fun showProgress()
    fun hideProgress()
}