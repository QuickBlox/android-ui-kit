/*
 * Created by Injoit on 12.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.screens.chat.group

import android.content.Context
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithAvatarComponent
import com.quickblox.android_ui_kit.presentation.components.header.HeaderWithAvatarComponentImpl
import com.quickblox.android_ui_kit.presentation.components.messages.MessagesComponent
import com.quickblox.android_ui_kit.presentation.components.messages.MessagesComponentImpl
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettings
import com.quickblox.android_ui_kit.presentation.screens.ScreenSettingsBuilder
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class GroupChatScreenSettings private constructor() : ScreenSettings {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var showHeader: Boolean = true
    private var showMessages: Boolean = true

    private var headerWithAvatarComponent: HeaderWithAvatarComponent? = null
    private var messagesComponent: MessagesComponent? = null

    fun isShowHeader(): Boolean {
        return showHeader
    }

    fun isShowMessages(): Boolean {
        return showMessages
    }

    fun getHeaderComponent(): HeaderWithAvatarComponent? {
        return headerWithAvatarComponent
    }

    fun getMessagesComponent(): MessagesComponent? {
        return messagesComponent
    }

    fun setHeaderComponent(component: HeaderWithAvatarComponent) {
        this.headerWithAvatarComponent = component
    }

    fun setNameComponent(component: MessagesComponent) {
        this.messagesComponent = component
    }

    override fun getTheme(): UiKitTheme {
        return theme
    }

    data class Builder(val context: Context) : ScreenSettingsBuilder<GroupChatScreenSettings> {
        private var theme: UiKitTheme = QuickBloxUiKit.getTheme()
        private var showHeader: Boolean = true
        private var showMessages: Boolean = true
        private var headerComponent: HeaderWithAvatarComponent? = null
        private var messagesComponent: Component? = null

        fun showHeader(show: Boolean): Builder {
            this.showHeader = show
            return this
        }

        fun showMessages(show: Boolean): Builder {
            this.showMessages = show
            return this
        }

        fun setHeaderComponent(component: HeaderWithAvatarComponent): Builder {
            this.headerComponent = component
            return this
        }

        fun setMessagesComponent(component: MessagesComponent): Builder {
            this.messagesComponent = component
            return this
        }

        fun setTheme(theme: UiKitTheme): Builder {
            this.theme = theme
            return this
        }

        override fun build(): GroupChatScreenSettings {
            val settings = GroupChatScreenSettings()

            if (headerComponent == null && showHeader) {
                val headerComponent = HeaderWithAvatarComponentImpl(context)
                headerComponent.setTheme(theme)

                settings.headerWithAvatarComponent = headerComponent
            }

            if (messagesComponent == null && showMessages) {
                val messagesComponent = MessagesComponentImpl(context)
                messagesComponent.setTheme(theme)
                settings.messagesComponent = messagesComponent
            }

            settings.theme = theme
            settings.showHeader = showHeader
            settings.showMessages = showMessages
            return settings
        }
    }
}