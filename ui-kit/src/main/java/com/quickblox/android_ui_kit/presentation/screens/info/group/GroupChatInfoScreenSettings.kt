/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.group

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithAvatarComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponentImpl
import com.quickblox.android_ui_kit.presentation.components.info.DialogInfoComponent
import com.quickblox.android_ui_kit.presentation.components.info.DialogInfoComponentImpl
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import java.io.Serializable

class GroupChatInfoScreenSettings private constructor() : ScreenSettings, Serializable {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showInfo: Boolean = true

    private var headerWithTextComponent: HeaderWithTextComponent? = null
    private var infoComponent: DialogInfoComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowInfo(): Boolean {
        return showInfo
    }

    fun getHeaderComponent(): HeaderWithTextComponent? {
        return headerWithTextComponent
    }

    fun getInfoComponent(): DialogInfoComponent? {
        return infoComponent
    }

    fun setHeaderComponent(component: HeaderWithTextComponent) {
        this.headerWithTextComponent = component
    }

    fun setInfoComponent(component: DialogInfoComponent) {
        this.infoComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<GroupChatInfoScreenSettings>, Serializable {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showInfo: Boolean = true
        private var headerComponent: HeaderWithAvatarComponent? = null
        private var infoComponent: DialogInfoComponent? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showInfo(show: Boolean): Builder {
            this.showInfo = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithAvatarComponent): Builder {
            this.headerComponent = component
            return this
        }

        fun setInfoComponent(component: DialogInfoComponent): Builder {
            this.infoComponent = component
            return this
        }

        fun setTheme(theme: UiKitTheme): Builder {
            this.theme = theme
            return this
        }

        override fun build(): GroupChatInfoScreenSettings {
            val settings = GroupChatInfoScreenSettings()

            if (headerComponent == null && showHeader) {
                val headerComponent = HeaderWithTextComponentImpl(context)
                headerComponent.setTheme(theme)

                settings.headerWithTextComponent = headerComponent
            }

            if (infoComponent == null && showInfo) {
                val infoComponent = DialogInfoComponentImpl(context)
                infoComponent.setTheme(theme)

                settings.infoComponent = infoComponent
            }

            settings.theme = theme
            settings.showHeader = showHeader
            settings.showInfo = showInfo
            return settings
        }
    }
}