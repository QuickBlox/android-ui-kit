/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens

import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import java.io.Serializable

interface ScreenSettings : Serializable {
    fun getTheme(): UiKitTheme
}