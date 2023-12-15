/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AIMenuDialog private constructor(
    context: Context, private val message: ForwardedRepliedMessageEntity, theme: UiKitTheme?, val listener: IncomingMessageMenuListener
) : BaseDialog(context, null, theme) {
    companion object {
        fun show(context: Context, message: ForwardedRepliedMessageEntity, theme: UiKitTheme?, listener: IncomingMessageMenuListener) {
            AIMenuDialog(context, message, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val answerItem = buildAnswerItem()
        views.add(answerItem)
        return views
    }

    private fun buildAnswerItem(): View {
        val answerItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            answerItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            answerItem.setRipple(it)
        }
        answerItem.setText(context.getString(R.string.menu_item_ai_answer_assistant))
        answerItem.setItemClickListener {
            listener.onAiAnswerAssistantClicked(message)
            dismiss()
        }

        return answerItem
    }

    interface IncomingMessageMenuListener {
        fun onAiAnswerAssistantClicked(message: ForwardedRepliedMessageEntity?)
    }
}