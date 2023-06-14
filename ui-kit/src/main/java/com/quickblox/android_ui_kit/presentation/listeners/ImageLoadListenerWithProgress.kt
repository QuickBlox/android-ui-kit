/*
 * Created by Injoit on 12.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.listeners

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R

class ImageLoadListenerWithProgress(
    private val imageView: AppCompatImageView,
    private val context: Context,
    private val progressBar: ProgressBar
) : RequestListener<Drawable> {
    init {
        progressBar.visibility = View.VISIBLE
    }

    override fun onLoadFailed(
        e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
    ): Boolean {
        val errorBg = ContextCompat.getDrawable(context, R.drawable.send_error) as VectorDrawable
        imageView.background = errorBg
        progressBar.visibility = View.GONE
        return true
    }

    override fun onResourceReady(
        resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean
    ): Boolean {
        progressBar.visibility = View.GONE
        return false
    }
}