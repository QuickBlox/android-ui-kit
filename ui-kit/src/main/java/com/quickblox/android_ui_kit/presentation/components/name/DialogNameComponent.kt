/*
 * Created by Injoit on 25.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.name

import android.text.TextWatcher
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.quickblox.android_ui_kit.presentation.components.Component

interface DialogNameComponent : Component {
    fun getNameHint(): String
    fun setNameHint(text: String?)
    fun setNameHintColor(@ColorInt color: Int)

    fun getNameText(): String
    fun setNameText(text: String?)
    fun setNameTextColor(@ColorInt color: Int)
    fun setTextWatcherToEditText(textWatcher: TextWatcher)

    fun getAvatarClickListener(): (() -> Unit)?
    fun getAvatarView(): AppCompatImageView?
    fun setAvatarClickListener(listener: (() -> Unit)?)
    fun setVisibleAvatar(visible: Boolean)

    fun setBackground(@ColorInt color: Int)
    fun setDefaultPlaceHolderAvatar()

    fun showAvatarProgress()
    fun hideAvatarProgress()
}