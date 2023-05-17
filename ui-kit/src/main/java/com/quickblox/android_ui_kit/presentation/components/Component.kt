/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components

import android.view.View
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

interface Component {
    fun setTheme(theme: UiKitTheme)
    fun getView(): View
}