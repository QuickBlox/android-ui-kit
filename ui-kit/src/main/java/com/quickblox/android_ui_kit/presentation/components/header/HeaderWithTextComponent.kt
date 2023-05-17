/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.header

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.quickblox.android_ui_kit.presentation.components.Component

interface HeaderWithTextComponent : Component {
    fun setTitle(title: String?)
    fun getTitle(): String?
    fun setTitleColor(@ColorInt color: Int)

    fun setLeftButtonClickListener(listener: (() -> Unit)?)
    fun getLeftButtonClickListener(): (() -> Unit)?
    fun setLeftButtonColor(@ColorInt color: Int)
    fun setImageLeftButton(@DrawableRes resource: Int)

    fun setRightButtonClickListener(listener: (() -> Unit)?)
    fun getRightButtonClickListener(): (() -> Unit)?
    fun setRightButtonClickableState()
    fun setRightButtonNotClickableState()
    fun setRightButtonTextColor(@ColorInt color: Int)
    fun setTextRightButton(text: String)

    fun setVisibleLeftButton(visible: Boolean)
    fun setVisibleRightButton(visible: Boolean)
    fun setVisibleTitle(visible: Boolean)

    fun setDividerColor(@ColorInt color: Int)
    fun setBackground(@ColorInt color: Int)

}