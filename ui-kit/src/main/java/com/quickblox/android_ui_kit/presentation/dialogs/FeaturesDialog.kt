/*
 * Created by Injoit on 3.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class FeaturesDialog private constructor(
    context: Context, theme: UiKitTheme?, private val listener: FeaturesListener,
) : BaseDialog(context, null, theme) {

    companion object {
        fun show(context: Context, theme: UiKitTheme?, listener: FeaturesListener) {
            FeaturesDialog(context, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val forwardItem = buildForwardItem()

        views.add(forwardItem)
        return views
    }

    private fun buildForwardItem(): View {
        val forwardItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            forwardItem.setColorText(it)
        }

        themeDialog?.getMainElementsColor()?.let {
            forwardItem.setRipple(it)
        }

        forwardItem.setText(context.getString(R.string.forward))

        forwardItem.setItemClickListener {
            dismiss()
            listener.onForward()
        }
        return forwardItem
    }

    interface FeaturesListener {
        fun onForward()
    }
}