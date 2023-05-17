/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.info.members

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithIconComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithIconComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.UsersComponent
import com.quickblox.android_ui_kit.presentation.components.users.UsersComponentImpl
import com.quickblox.android_ui_kit.presentation.components.users.members.MembersAdapter
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class MembersScreenSettings private constructor() : ScreenSettings {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showName: Boolean = true

    private var headerComponent: HeaderWithIconComponent? = null
    private var usersComponent: UsersComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowUsers(): Boolean {
        return showName
    }

    fun getHeaderComponent(): HeaderWithIconComponent? {
        return headerComponent
    }

    fun getUsersComponent(): UsersComponent? {
        return usersComponent
    }

    fun setHeaderComponent(component: HeaderWithIconComponent) {
        this.headerComponent = component
    }

    fun setUsersComponent(component: UsersComponent) {
        this.usersComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<MembersScreenSettings> {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showUsers: Boolean = true
        private var headerComponent: HeaderWithIconComponent? = null
        private var usersComponent: UsersComponent? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showUsers(show: Boolean): Builder {
            this.showUsers = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithIconComponent): Builder {
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

        override fun build(): MembersScreenSettings {
            val settings = MembersScreenSettings()

            if (headerComponent == null && showHeader) {
                val headerComponent = HeaderWithIconComponentImpl(context)
                headerComponent.setTheme(theme)

                settings.headerComponent = headerComponent
            }
            if (usersComponent == null && showUsers) {
                val adapter = MembersAdapter()
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