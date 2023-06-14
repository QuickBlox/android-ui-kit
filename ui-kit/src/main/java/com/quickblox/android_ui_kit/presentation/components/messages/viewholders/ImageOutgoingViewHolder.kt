/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.CENTER
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ImageOutgiongMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.OutgoingChatMessageEntity.OutgoingStates
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class ImageOutgoingViewHolder(binding: ImageOutgiongMessageItemBinding) :
    BaseViewHolder<ImageOutgiongMessageItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()

    companion object {
        fun newInstance(parent: ViewGroup): ImageOutgoingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ImageOutgoingViewHolder(ImageOutgiongMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(message: OutgoingChatMessageEntity?, listener: ImageOutgoingListener?) {
        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        applyPlaceHolder(message?.getMediaContent()?.isGif())

        // TODO: Need to refactor
        val isNotSendingState = message?.getOutgoingState() != OutgoingStates.SENDING
        if (isNotSendingState) {
            loadImageFrom(message)
        }

        setListener(message, listener)
        setState(message)

        applyTheme(theme)
    }

    private fun setListener(message: OutgoingChatMessageEntity?, listener: ImageOutgoingListener?) {
        binding.ivImage.setOnClickListener {
            listener?.onImageClick(message)
        }

        binding.ivImage.setOnLongClickListener {
            listener?.onImageLongClick(message)
            true
        }
    }

    private fun loadImageFrom(message: OutgoingChatMessageEntity?) {
        val context = binding.root.context
        val url = message?.getMediaContent()?.getUrl()
        val holderId: Int
        if (message?.getMediaContent()?.isGif() == true) {
            holderId = R.drawable.ic_gif_placeholder
        } else {
            holderId = R.drawable.ic_image_placeholder
        }

        Glide.with(context).load(url).placeholder(ContextCompat.getDrawable(binding.root.context, holderId))
            .listener(RequestListenerImpl(message)).into(binding.ivImage)
    }

    private fun applyTheme(theme: UiKitTheme) {
        setTimeTextColor(theme.getTertiaryElementsColor())
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    private fun applyPlaceHolder(isGif: Boolean?) {
        val drawable =
            ContextCompat.getDrawable(binding.root.context, R.drawable.outgoing_media_placeholder) as GradientDrawable
        drawable.setColor(theme.getOutgoingMessageColor())
        binding.ivImage.background = drawable

        if (isGif == true) {
            binding.ivImage.setImageResource(R.drawable.ic_gif_placeholder)
        } else {
            binding.ivImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        binding.ivImage.scaleType = CENTER
    }

    private fun setState(message: OutgoingChatMessageEntity?) {
        val resourceId: Int?
        val color: Int?
        when (message?.getOutgoingState()) {
            OutgoingStates.SENDING -> {
                resourceId = R.drawable.sending
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.SENT -> {
                resourceId = R.drawable.sent
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.DELIVERED -> {
                resourceId = R.drawable.delivered
                color = theme.getTertiaryElementsColor()
            }
            OutgoingStates.READ -> {
                resourceId = R.drawable.read
                color = theme.getMainElementsColor()
            }
            OutgoingStates.ERROR -> {
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

    private inner class RequestListenerImpl(private val message: OutgoingChatMessageEntity?) :
        RequestListener<Drawable> {
        init {
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean,
        ): Boolean {
            applyPlaceHolder(message?.getMediaContent()?.isGif())
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

    interface ImageOutgoingListener {
        fun onImageClick(message: OutgoingChatMessageEntity?)
        fun onImageLongClick(message: OutgoingChatMessageEntity?)
    }
}