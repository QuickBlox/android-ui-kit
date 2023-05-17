/*
 * Created by Injoit on 4.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.screens.chat.full_image_screen

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.FullImageScreenBinding
import com.quickblox.android_ui_kit.presentation.base.BaseActivity
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

private const val URL_EXTRA = "url"
private const val IS_GIF_EXTRA = "is_gif"

class FullImageScreenActivity : BaseActivity() {
    private lateinit var binding: FullImageScreenBinding

    companion object {
        fun show(context: Context, url: String?, isGif: Boolean = false) {
            val intent = Intent(context, FullImageScreenActivity::class.java)
            intent.putExtra(URL_EXTRA, url)
            intent.putExtra(IS_GIF_EXTRA, isGif)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FullImageScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val theme = QuickBloxUiKit.getTheme()
        window.statusBarColor = theme.getStatusBarColor()

        val url = intent.getStringExtra(URL_EXTRA)
        val isGif = intent.getBooleanExtra(IS_GIF_EXTRA, false)
        if (isGif) {
            Glide.with(this).asGif().load(url).listener(RequestGifListenerImpl(theme)).into(binding.ivImage)
        } else {
            Glide.with(this).load(url).listener(RequestImageListenerImpl(theme)).into(binding.ivImage)
        }

        binding.btnLeft.setOnClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnLeft.makeClickableBackground()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun applyPlaceHolder(theme: UiKitTheme, holderId: Int) {
        binding.ivImage.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder)
        binding.ivImage.backgroundTintList = ColorStateList.valueOf(theme.getOutgoingMessageColor())

        binding.ivImage.setImageResource(holderId)
        binding.ivImage.setColorFilter(theme.getSecondaryElementsColor())
        binding.ivImage.scaleType = ImageView.ScaleType.CENTER
    }

    private inner class RequestImageListenerImpl(private val theme: UiKitTheme) : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder(theme, R.drawable.ic_image_placeholder)
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            return false
        }
    }

    private inner class RequestGifListenerImpl(private val theme: UiKitTheme) : RequestListener<GifDrawable> {
        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder(theme, R.drawable.ic_gif_placeholder)
            return true
        }

        override fun onResourceReady(
            resource: GifDrawable?,
            model: Any?,
            target: Target<GifDrawable>?,
            dataSource: DataSource?,
            isFirst: Boolean,
        ): Boolean {
            return false
        }
    }
}