/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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
import com.quickblox.android_ui_kit.databinding.VideoIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class VideoIncomingViewHolder(binding: VideoIncomingMessageItemBinding) :
    BaseViewHolder<VideoIncomingMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): VideoIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return VideoIncomingViewHolder(VideoIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: IncomingChatMessageEntity, listener: VideoIncomingListener?) {
        binding.tvTime.text = message.getTime()?.convertToStringTime()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = message.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            binding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        applyPlaceHolder()

        loadImageBy(message.getMediaContent()?.getUrl())

        binding.tvName.text = sender?.getName() ?: sender?.getLogin()

        setListener(message, listener)

        applyTheme(theme)
    }

    private fun setListener(message: IncomingChatMessageEntity?, listener: VideoIncomingListener?) {
        binding.ivVideo.setOnClickListener {
            listener?.onVideoClick(message)
        }

        binding.ivVideo.setOnLongClickListener {
            listener?.onVideoLongClick(message)
            true
        }
    }

    private fun loadImageBy(url: String?) {
        val context = binding.root.context

        Glide.with(context).load(url)
            .placeholder(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_video_placeholder)).frame(0)
            .listener(RequestListenerImpl()).into(binding.ivVideo)
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

    private fun applyPlaceHolder() {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.incoming_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getIncomingMessageColor())
        binding.ivVideo.background = drawable

        binding.ivVideo.setImageResource(R.drawable.ic_video_placeholder)

        binding.ivVideo.scaleType = ImageView.ScaleType.CENTER
    }

    private inner class RequestListenerImpl : RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
            binding.ivPlayButton.visibility = View.GONE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder()
            binding.progressBar.visibility = View.GONE
            return true
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirst: Boolean,
        ): Boolean {
            binding.progressBar.visibility = View.GONE
            binding.ivPlayButton.visibility = View.VISIBLE

            setPlayButtonBackground()
            return false
        }
    }

    private fun setPlayButtonBackground() {
        val playButtonDrawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.bg_around_corners_6dp) as GradientDrawable
        playButtonDrawable.setColor(Color.parseColor("#99131D28"))
        binding.ivPlayButton.background = playButtonDrawable
    }

    interface VideoIncomingListener {
        fun onVideoClick(message: IncomingChatMessageEntity?)
        fun onVideoLongClick(message: IncomingChatMessageEntity?)
    }
}