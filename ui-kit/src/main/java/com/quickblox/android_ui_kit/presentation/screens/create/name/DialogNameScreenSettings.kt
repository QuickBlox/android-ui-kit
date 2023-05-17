/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.screens.create.name

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponentImpl
import com.quickblox.android_ui_kit.presentation.components.name.DialogNameComponent
import com.quickblox.android_ui_kit.presentation.components.name.DialogNameComponentImpl
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class DialogNameScreenSettings private constructor() : ScreenSettings {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showName: Boolean = true

    private var headerWithTextComponent: HeaderWithTextComponent? = null
    private var nameComponent: DialogNameComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowName(): Boolean {
        return showName
    }

    fun getHeaderComponent(): HeaderWithTextComponent? {
        return headerWithTextComponent
    }

    fun getNameComponent(): DialogNameComponent? {
        return nameComponent
    }

    fun setHeaderComponent(component: HeaderWithTextComponent) {
        this.headerWithTextComponent = component
    }

    fun setNameComponent(component: DialogNameComponent) {
        this.nameComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<DialogNameScreenSettings> {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showName: Boolean = true
        private var headerComponent: HeaderWithTextComponent? = null
        private var nameComponent: DialogNameComponent? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showName(show: Boolean): Builder {
            this.showName = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithTextComponent): Builder {
            this.headerComponent = component
            return this
        }

        fun setNameComponent(component: DialogNameComponent): Builder {
            this.nameComponent = component
            return this
        }

        fun setTheme(theme: UiKitTheme): Builder {
            this.theme = theme
            return this
        }

        override fun build(): DialogNameScreenSettings {
            val settings = DialogNameScreenSettings()

            if (headerComponent == null && showHeader) {
                val headerComponent = HeaderWithTextComponentImpl(context)
                headerComponent.setTheme(theme)

                settings.headerWithTextComponent = headerComponent
            }
            if (nameComponent == null && showName) {
                val nameComponent = DialogNameComponentImpl(context)
                nameComponent.setTheme(theme)

                settings.nameComponent = nameComponent
            }

            settings.theme = theme
            settings.showHeader = showHeader
            settings.showName = showName
            return settings
        }
    }
}