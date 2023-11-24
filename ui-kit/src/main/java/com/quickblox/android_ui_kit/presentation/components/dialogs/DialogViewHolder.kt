/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogGroupItemBinding
import com.quickblox.android_ui_kit.databinding.DialogMediaMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.PRIVATE
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MediaContentEntity
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.listeners.ImageLoadListenerWithProgress
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.screens.modifyDialogsDateStringFrom
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogViewHolder(binding: DialogGroupItemBinding) : BaseViewHolder<DialogGroupItemBinding>(binding) {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar: Boolean = true
    private var isVisibleCounter: Boolean = true
    private var lastMessageColor: Int = theme.getSecondaryTextColor()

    companion object {
        fun newInstance(parent: ViewGroup): DialogViewHolder {
            return DialogViewHolder(
                DialogGroupItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun bind(dialog: DialogEntity) {
        applyTheme(theme)

        binding.tvDialogName.text = dialog.getName()

        showUnreadMessageCounter(dialog.getUnreadMessagesCount())

        showMessage(dialog.getLastMessage())

        dialog.getLastMessage()?.getTime()?.let { time ->
            val modifiedTime = modifyDialogsDateStringFrom(time)
            binding.tvTime.text = modifiedTime
        }

        if (dialog.getType() == PRIVATE) {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.private_holder)
        } else {
            binding.ivAvatar.loadCircleImageFromUrl(dialog.getPhoto(), R.drawable.group_holder)
        }
    }

    private fun showMessage(message: IncomingChatMessageEntity?) {
        binding.flLastMessage.removeAllViews()

        val isMediaMessage = message?.isMediaContent() == true
        if (isMediaMessage) {
            showMediaMessage(message?.getMediaContent())
        } else {
            showTextMessage(message?.getContent())
        }
    }

    private fun showMediaMessage(mediaContentEntity: MediaContentEntity?) {
        val mediaMessageBinding = buildMediaMessageBinding()

        mediaMessageBinding.tvFileName.text = mediaContentEntity?.getName()
        mediaMessageBinding.tvFileName.setTextColor(lastMessageColor)

        val resourceId = getResourceIdByMediaContent(mediaContentEntity?.getType())

        val context = binding.root.context
        mediaMessageBinding.ivMediaIcon.background = ContextCompat.getDrawable(context, R.drawable.bg_media_message)
        mediaMessageBinding.ivMediaIcon.setImageResource(resourceId)

        if (mediaContentEntity?.isImage() == true) {
            val progressBar = mediaMessageBinding.progressBar

            val backgroundImageView = mediaMessageBinding.ivMediaIcon
            backgroundImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            loadImageByUrl(mediaContentEntity.getUrl(), backgroundImageView, progressBar)
        }

        binding.flLastMessage.addView(mediaMessageBinding.root)
    }

    private fun loadImageByUrl(url: String?, imageView: AppCompatImageView, progressBar: ProgressBar) {
        val context = binding.root.context

        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
            .listener(ImageLoadListenerWithProgress(imageView, context, progressBar))
            .into(imageView)
    }

    private fun getResourceIdByMediaContent(contentType: MediaContentEntity.Types?): Int {
        when (contentType) {
            MediaContentEntity.Types.IMAGE -> {
                return R.drawable.ic_image_placeholder
            }
            MediaContentEntity.Types.VIDEO -> {
                return R.drawable.ic_video_file
            }
            MediaContentEntity.Types.AUDIO -> {
                return R.drawable.ic_audio_file
            }
            MediaContentEntity.Types.FILE -> {
                return R.drawable.ic_application_file
            }
            else -> {
                throw IllegalArgumentException("$contentType - type does not exist for media content")
            }
        }
    }

    private fun buildMediaMessageBinding(): DialogMediaMessageItemBinding {
        val inflater = LayoutInflater.from(binding.root.context)

        return DialogMediaMessageItemBinding.inflate(inflater)
    }

    private fun showTextMessage(text: String?) {
        val textView = buildMessageTextView()

        if (text?.contains("[Forwarded_Message]") == true) {
            textView.text = "Forwarded message"
        } else {
            textView.text = text
        }

        binding.flLastMessage.addView(textView)
    }

    private fun showUnreadMessageCounter(counter: Int?) {
        if (counter == null || counter == 0) {
            setVisibleCounter(false)
        } else {
            setVisibleCounter(true)
            binding.tvCounter.text = counter.toString()
        }
    }

    private fun buildMessageTextView(): AppCompatTextView {
        val textView = AppCompatTextView(binding.root.context)
        textView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary500))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textView.setTextColor(lastMessageColor)
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setLines(2)

        return textView
    }

    private fun applyTheme(theme: UiKitTheme) {
        setDividerColor(theme.getDividerColor())
        setCounterTextColor(theme.getMainBackgroundColor())
        setCounterBackgroundColor(theme.getMainElementsColor())
        setTimeColor(theme.getSecondaryTextColor())
        setLastMessageColor(theme.getSecondaryTextColor())
        setDialogNameColor(theme.getSecondaryElementsColor())
        binding.root.makeClickableBackground(theme.getMainElementsColor())
    }

    fun setDialogNameColor(@ColorInt color: Int) {
        binding.tvDialogName.setTextColor(color)
    }

    fun setLastMessageColor(@ColorInt color: Int) {
        this.lastMessageColor = color
    }

    fun setTimeColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    @SuppressLint("RestrictedApi")
    fun setCounterBackgroundColor(@ColorInt color: Int) {
        binding.tvCounter.supportBackgroundTintList = ColorStateList.valueOf(color)
    }

    fun setCounterTextColor(@ColorInt color: Int) {
        binding.tvCounter.setTextColor(color)
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.vDivider.setBackgroundColor(color)
    }

    fun setVisibleAvatar(visible: Boolean) {
        isVisibleAvatar = visible
        binding.ivAvatar.setVisibility(visible)
    }

    fun setVisibleLastMessage(visible: Boolean) {
        binding.flLastMessage.setVisibility(visible)
    }

    fun setVisibleTime(visible: Boolean) {
        binding.tvTime.setVisibility(visible)
    }

    fun setVisibleCounter(visible: Boolean) {
        isVisibleCounter = visible
        binding.tvCounter.setVisibility(visible)
    }
}
