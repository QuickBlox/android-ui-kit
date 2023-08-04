/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AIMenuDialog private constructor(
    context: Context, private val message: IncomingChatMessageEntity, theme: UiKitTheme?, val listener: IncomingMessageMenuListener
) : BaseDialog(context, null, theme) {
    companion object {
        fun show(context: Context, message: IncomingChatMessageEntity, theme: UiKitTheme?, listener: IncomingMessageMenuListener) {
            AIMenuDialog(context, message, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val liveItem = buildLeaveItem()
        views.add(liveItem)
        return views
    }

    private fun buildLeaveItem(): View {
        val liveItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            liveItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            liveItem.setRipple(it)
        }
        liveItem.setText(context.getString(R.string.menu_item_ai_answer_assistant))
        liveItem.setItemClickListener {
            listener.onAiAnswerAssistantClicked(message)
            dismiss()
        }

        return liveItem
    }

    interface IncomingMessageMenuListener {
        fun onAiAnswerAssistantClicked(message: IncomingChatMessageEntity?)
    }
}