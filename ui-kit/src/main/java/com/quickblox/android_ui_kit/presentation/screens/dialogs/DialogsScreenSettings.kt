/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.dialogs

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsComponent
import com.quickblox.android_ui_kit.presentation.components.dialogs.DialogsComponentImpl
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithIconComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithIconComponentImpl
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponent
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogsScreenSettings private constructor() : ScreenSettings {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showSearch: Boolean = true
    private var showDialogs: Boolean = true

    private var headerWithIconComponent: HeaderWithIconComponent? = null
    private var dialogsComponent: DialogsComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowSearch(): Boolean {
        return showSearch
    }

    fun isShowDialogs(): Boolean {
        return showDialogs
    }

    fun getHeaderComponent(): HeaderWithIconComponent? {
        return headerWithIconComponent
    }

    fun getSearchComponent(): SearchComponent? {
        return dialogsComponent?.getSearchComponent()
    }

    fun getDialogsComponent(): DialogsComponent? {
        return dialogsComponent
    }

    fun setHeaderComponent(component: HeaderWithIconComponent) {
        this.headerWithIconComponent = component
    }

    fun setDialogsComponent(component: DialogsComponent) {
        this.dialogsComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<DialogsScreenSettings> {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showSearch: Boolean = true
        private var showDialogs: Boolean = true
        private var headerComponent: HeaderWithIconComponent? = null
        private var searchComponent: SearchComponent? = null
        private var dialogsComponent: DialogsComponent? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showSearch(show: Boolean): Builder {
            this.showSearch = show
            return this
        }

        fun showDialogs(show: Boolean): Builder {
            this.showDialogs = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithIconComponent): Builder {
            this.headerComponent = component
            return this
        }

        fun setSearchComponent(component: SearchComponent): Builder {
            this.searchComponent = component
            return this
        }

        fun setDialogsComponent(component: DialogsComponent): Builder {
            this.dialogsComponent = component
            return this
        }

        fun setTheme(theme: UiKitTheme): Builder {
            this.theme = theme
            return this
        }

        override fun build(): DialogsScreenSettings {
            val settings = DialogsScreenSettings()

            if (showHeader) {
                if (headerComponent == null) {
                    val headerComponent = HeaderWithIconComponentImpl(context)
                    headerComponent.setTheme(theme)
                    settings.headerWithIconComponent = headerComponent
                } else {
                    settings.headerWithIconComponent = headerComponent
                }
            }

            if (showDialogs) {
                if (dialogsComponent == null) {
                    val localDialogsComponent = DialogsComponentImpl(context)
                    localDialogsComponent.setTheme(theme)
                    localDialogsComponent.showSearch(showSearch)
                    settings.dialogsComponent = localDialogsComponent
                } else {
                    settings.dialogsComponent = dialogsComponent
                }
            }

            settings.theme = theme
            settings.showHeader = showHeader
            settings.showSearch = showSearch
            settings.showDialogs = showDialogs
            return settings
        }
    }
}