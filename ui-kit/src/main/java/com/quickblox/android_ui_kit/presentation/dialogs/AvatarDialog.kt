/*
 * Created by Injoit on 26.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AvatarDialog constructor(
    context: Context, title: String, theme: UiKitTheme?, private val listener: AvatarDialogListener
) : BaseDialog(context, title, theme) {
    companion object {
        fun show(context: Context, title: String, theme: UiKitTheme?, listener: AvatarDialogListener) {
            AvatarDialog(context, title, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val cameraItem = buildCameraItem()
        val galleryItem = buildGalleryItem()
        val removeItem = buildRemoveItem()

        views.add(cameraItem)
        views.add(galleryItem)
        views.add(removeItem)

        return views
    }

    private fun buildCameraItem(): View {
        val cameraItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            cameraItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            cameraItem.setRipple(it)
        }
        cameraItem.setText(context.getString(R.string.camera))
        cameraItem.setItemClickListener {
            dismiss()
            listener.onClickCamera()
        }

        return cameraItem
    }

    private fun buildGalleryItem(): View {
        val galleryItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            galleryItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            galleryItem.setRipple(it)
        }
        galleryItem.setText(context.getString(R.string.gallery))
        galleryItem.setItemClickListener {
            dismiss()
            listener.onClickGallery()
        }

        return galleryItem
    }

    private fun buildRemoveItem(): View {
        val removeItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            removeItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            removeItem.setRipple(it)
        }
        removeItem.setText(context.getString(R.string.remove_photo))
        removeItem.setItemClickListener {
            dismiss()
            listener.onClickRemove()
        }

        return removeItem
    }

    interface AvatarDialogListener {
        fun onClickCamera()
        fun onClickGallery()
        fun onClickRemove()
    }
}