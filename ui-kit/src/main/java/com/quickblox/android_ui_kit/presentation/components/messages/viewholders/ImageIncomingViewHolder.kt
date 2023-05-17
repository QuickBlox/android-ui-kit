/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ImageIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class ImageIncomingViewHolder(binding: ImageIncomingMessageItemBinding) :
    BaseViewHolder<ImageIncomingMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): ImageIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ImageIncomingViewHolder(ImageIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: IncomingChatMessageEntity, listener: ImageIncomingListener?) {
        binding.tvTime.text = message.getTime()?.convertToStringTime()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = message.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            binding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        applyPlaceHolder(message.getMediaContent()?.isGif())

        loadImageFrom(message)

        binding.tvName.text = sender?.getName() ?: sender?.getLogin()

        setListener(message, listener)

        applyTheme(theme)
    }

    private fun setListener(message: IncomingChatMessageEntity?, listener: ImageIncomingListener?) {
        binding.ivImage.setOnClickListener {
            listener?.onImageClick(message)
        }

        binding.ivImage.setOnLongClickListener {
            listener?.onImageLongClick(message)
            true
        }
    }

    private fun loadImageFrom(message: IncomingChatMessageEntity) {
        val context = binding.root.context
        val url = message.getMediaContent()?.getUrl()
        val holderId: Int
        if (message.getMediaContent()?.isGif() == true) {
            holderId = R.drawable.ic_gif_placeholder
        } else {
            holderId = R.drawable.ic_image_placeholder
        }

        Glide.with(context).load(url).placeholder(ContextCompat.getDrawable(binding.root.context, holderId))
            .listener(RequestListenerImpl(message)).into(binding.ivImage)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setNameColor(theme.getTertiaryElementsColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun applyPlaceHolder(isGif: Boolean?) {
        binding.ivImage.background =
            ContextCompat.getDrawable(binding.root.context, R.drawable.incoming_media_placeholder)
        binding.ivImage.backgroundTintList = ColorStateList.valueOf(theme.getIncomingMessageColor())

        if (isGif == true) {
            binding.ivImage.setImageResource(R.drawable.ic_gif_placeholder)
        } else {
            binding.ivImage.setImageResource(R.drawable.ic_image_placeholder)
        }
        binding.ivImage.scaleType = ImageView.ScaleType.CENTER
    }

    private inner class RequestListenerImpl(private val message: IncomingChatMessageEntity) :
        RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder(message.getMediaContent()?.isGif())
            binding.progressBar.visibility = View.GONE

            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            binding.progressBar.visibility = View.GONE

            return false
        }
    }

    interface ImageIncomingListener {
        fun onImageClick(message: IncomingChatMessageEntity?)
        fun onImageLongClick(message: IncomingChatMessageEntity?)
    }
}