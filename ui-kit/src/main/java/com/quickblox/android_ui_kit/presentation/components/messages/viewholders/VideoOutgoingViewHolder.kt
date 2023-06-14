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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.VideoOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class VideoOutgoingViewHolder(binding: VideoOutgiongMessageItemBinding) :
    BaseViewHolder<VideoOutgiongMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): VideoOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return VideoOutgoingViewHolder(VideoOutgiongMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: OutgoingChatMessageEntity?, listener: VideoOutgoingListener?) {
        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        applyPlaceHolder()

        // TODO: Need to refactor
        val isNotSendingState = message?.getOutgoingState() != OutgoingChatMessageEntity.OutgoingStates.SENDING
        if (isNotSendingState) {
            loadImageBy(message?.getMediaContent()?.getUrl())
        }

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    private fun setListener(message: OutgoingChatMessageEntity?, listener: VideoOutgoingListener?) {
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

        val placeHolder: Drawable?
        val isDrawableNotExist = binding.ivVideo.drawable == null
        if (isDrawableNotExist) {
            placeHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_video_placeholder)
        } else {
            placeHolder = binding.ivVideo.drawable
        }

        val requestOptions = RequestOptions().frame(0)

        Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).placeholder(placeHolder)
            .listener(RequestListenerImpl()).into(binding.ivVideo)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setTimeTextColor(theme.getTertiaryElementsColor())
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun applyPlaceHolder() {
        val itemBackgroundDrawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        itemBackgroundDrawable.setColor(theme.getOutgoingMessageColor())
        binding.ivVideo.background = itemBackgroundDrawable

        binding.ivVideo.setImageResource(R.drawable.ic_video_placeholder)

        binding.ivVideo.scaleType = ImageView.ScaleType.CENTER
    }

    private fun setState(message: OutgoingChatMessageEntity?) {
        val resourceId: Int?
        val color: Int?
        when (message?.getOutgoingState()) {
            OutgoingChatMessageEntity.OutgoingStates.SENDING -> {
                resourceId = R.drawable.sending
                color = theme.getTertiaryElementsColor()
            }
            OutgoingChatMessageEntity.OutgoingStates.SENT -> {
                resourceId = R.drawable.sent
                color = theme.getTertiaryElementsColor()
            }
            OutgoingChatMessageEntity.OutgoingStates.DELIVERED -> {
                resourceId = R.drawable.delivered
                color = theme.getTertiaryElementsColor()
            }
            OutgoingChatMessageEntity.OutgoingStates.READ -> {
                resourceId = R.drawable.read
                color = theme.getMainElementsColor()
            }
            OutgoingChatMessageEntity.OutgoingStates.ERROR -> {
                resourceId = R.drawable.send_error
                color = theme.getErrorColor()
            }
            else -> {
                return
            }
        }

        binding.ivStatus.setImageResource(resourceId)
        binding.ivStatus.setColorFilter(color)
    }

    private inner class RequestListenerImpl : RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
            binding.ivPlayButton.visibility = View.GONE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
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

    interface VideoOutgoingListener {
        fun onVideoClick(message: OutgoingChatMessageEntity?)
        fun onVideoLongClick(message: OutgoingChatMessageEntity?)
    }
}