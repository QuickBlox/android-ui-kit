/*
 * Created by Injoit on 14.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class EditDialog constructor(
    context: Context, title: String, theme: UiKitTheme?, private val listener: EditDialogListener
) : BaseDialog(context, title, theme) {
    companion object {
        fun show(context: Context, title: String, theme: UiKitTheme?, listener: EditDialogListener) {
            EditDialog(context, title, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val editPhotoItem = buildEditPhotoItem()
        val editNameItem = buildEditNameItem()

        views.add(editPhotoItem)
        views.add(editNameItem)
        return views
    }

    private fun buildEditPhotoItem(): View {
        val cameraItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            cameraItem.setColorText(it)
        }

        themeDialog?.getMainElementsColor()?.let {
            cameraItem.setRipple(it)
        }

        cameraItem.setText(context.getString(R.string.change_photo))

        cameraItem.setItemClickListener {
            dismiss()
            listener.onClickPhoto()
        }
        return cameraItem
    }

    private fun buildEditNameItem(): View {
        val galleryItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            galleryItem.setColorText(it)
        }

        themeDialog?.getMainElementsColor()?.let {
            galleryItem.setRipple(it)
        }

        galleryItem.setText(context.getString(R.string.change_dialog_name))

        galleryItem.setItemClickListener {
            dismiss()
            listener.onClickName()
        }
        return galleryItem
    }

    interface EditDialogListener {
        fun onClickPhoto()
        fun onClickName()
    }
}