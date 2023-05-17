/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.screens.create.users

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithTextComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.UsersComponent
import com.quickblox.android_ui_kit.presentation.components.users.UsersComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.selection.SelectionUsersAdapter
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class UsersScreenSettings private constructor() : ScreenSettings {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showName: Boolean = true

    private var headerWithTextComponent: HeaderWithTextComponent? = null
    private var usersComponent: UsersComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowUsers(): Boolean {
        return showName
    }

    fun getHeaderComponent(): HeaderWithTextComponent? {
        return headerWithTextComponent
    }

    fun getUsersComponent(): UsersComponent? {
        return usersComponent
    }

    fun setHeaderComponent(component: HeaderWithTextComponent) {
        this.headerWithTextComponent = component
    }

    fun setUsersComponent(component: UsersComponent) {
        this.usersComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<UsersScreenSettings> {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showUsers: Boolean = true
        private var headerComponent: HeaderWithTextComponent? = null
        private var usersComponent: UsersComponent? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showUsers(show: Boolean): Builder {
            this.showUsers = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithTextComponent): Builder {
            this.headerComponent = component
            return this
        }

        fun setUsersComponent(component: UsersComponent): Builder {
            this.usersComponent = component
            return this
        }

        fun setTheme(theme: UiKitTheme): Builder {
            this.theme = theme
            return this
        }

        override fun build(): UsersScreenSettings {
            val settings = UsersScreenSettings()

            if (headerComponent == null && showHeader) {
                val headerComponent = HeaderWithTextComponentImpl(context)
                headerComponent.setTheme(theme)
                headerComponent.setTextRightButton(context.getString(R.string.create))

                settings.headerWithTextComponent = headerComponent
            }
            if (usersComponent == null && showUsers) {
                val adapter = SelectionUsersAdapter()
                adapter.setTheme(theme)

                val usersComponent = UsersComponentImpl(context)
                usersComponent.setTheme(theme)
                usersComponent.setAdapter(adapter)

                settings.usersComponent = usersComponent
            }

            settings.theme = theme
            settings.showHeader = showHeader
            settings.showName = showUsers
            return settings
        }
    }
}