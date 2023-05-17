/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class MenuDialog private constructor(
    context: Context, private val dialog: DialogEntity, theme: UiKitTheme?, val listener: DialogMenuListener
) : BaseDialog(context, dialog.getName(), theme) {
    companion object {
        fun show(context: Context, dialog: DialogEntity, theme: UiKitTheme?, listener: DialogMenuListener) {
            MenuDialog(context, dialog, theme, listener).show()
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
        liveItem.setText(context.getString(R.string.leave_dialog))
        liveItem.setItemClickListener {
            listener.onClickLeave(dialog)
            dismiss()
        }

        return liveItem
    }

    interface DialogMenuListener {
        fun onClickLeave(dialog: DialogEntity)
    }
}