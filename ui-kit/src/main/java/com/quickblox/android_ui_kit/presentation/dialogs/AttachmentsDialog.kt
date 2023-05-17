/*
 * Created by Injoit on 8.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.view.View
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.base.BaseDialog
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AttachmentsDialog constructor(
    context: Context, title: String, theme: UiKitTheme?, private val listener: AttachmentsDialogListener
) : BaseDialog(context, title, theme) {
    companion object {
        fun show(context: Context, title: String, theme: UiKitTheme?, listener: AttachmentsDialogListener) {
            AttachmentsDialog(context, title, theme, listener).show()
        }
    }

    override fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val photoCameraItem = buildPhotoCameraItem()
        val videoCameraItem = buildVideoCameraItem()
        val photoAndVideoItem = buildPhotoAndVideoGalleryItem()
        val fileItem = buildFileItem()

        views.add(photoCameraItem)
        views.add(videoCameraItem)
        views.add(photoAndVideoItem)
        views.add(fileItem)

        return views
    }

    private fun buildPhotoCameraItem(): View {
        val cameraItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            cameraItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            cameraItem.setRipple(it)
        }
        cameraItem.setText(context.getString(R.string.take_photo))
        cameraItem.setItemClickListener {
            dismiss()
            listener.onClickPhotoCamera()
        }

        return cameraItem
    }

    private fun buildVideoCameraItem(): View {
        val cameraItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            cameraItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            cameraItem.setRipple(it)
        }
        cameraItem.setText(context.getString(R.string.take_video))
        cameraItem.setItemClickListener {
            dismiss()
            listener.onClickVideoCamera()
        }

        return cameraItem
    }

    private fun buildPhotoAndVideoGalleryItem(): View {
        val galleryItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            galleryItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            galleryItem.setRipple(it)
        }
        galleryItem.setText(context.getString(R.string.photo_video))
        galleryItem.setItemClickListener {
            dismiss()
            listener.onClickPhotoAndVideoGallery()
        }

        return galleryItem
    }

    private fun buildFileItem(): View {
        val fileItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            fileItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            fileItem.setRipple(it)
        }
        fileItem.setText(context.getString(R.string.file))
        fileItem.setItemClickListener {
            dismiss()
            listener.onClickFile()
        }

        return fileItem
    }

    interface AttachmentsDialogListener {
        fun onClickPhotoCamera()
        fun onClickVideoCamera()
        fun onClickPhotoAndVideoGallery()
        fun onClickFile()
    }
}