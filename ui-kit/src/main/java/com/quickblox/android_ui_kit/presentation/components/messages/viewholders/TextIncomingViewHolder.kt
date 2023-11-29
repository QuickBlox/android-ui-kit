/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.*
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.TextIncomingMessageItemBinding
import com.quickblox.android_ui_kit.domain.entity.implementation.message.AITranslateIncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.ForwardedRepliedMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.IncomingChatMessageEntity
import com.quickblox.android_ui_kit.domain.entity.message.MessageEntity
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter
import com.quickblox.android_ui_kit.presentation.screens.convertToStringTime
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class TextIncomingViewHolder(binding: TextIncomingMessageItemBinding) :
    BaseMessageViewHolder<TextIncomingMessageItemBinding>(binding), Forward {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isEnabledAITranslate = true
    private var isEnabledAIAnswerAssistant = true
    private var checkBoxListener: MessageAdapter.CheckBoxListener? = null
    private var message: ForwardedRepliedMessageEntity? = null

    companion object {
        fun newInstance(parent: ViewGroup): TextIncomingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TextIncomingViewHolder(TextIncomingMessageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun clearCachedData() {
        binding.flForwardReplyContainer.removeAllViews()
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme(theme)
    }

    fun bind(
        message: ForwardedRepliedMessageEntity?,
        textListener: MessageListener?,
        aiListener: AIListener?,
        isForwardState: Boolean,
        selectedMessages: MutableList<MessageEntity>,
    ) {
        this.message = message
        if (message?.isForwardedOrReplied() == true) {
            setSelectedMessages(selectedMessages)
            showForwardedReplyMessages(
                message,
                textListener,
                theme,
                isForwardState,
                aiListener
            )

            val content = message.getContent()
            if (content.isNullOrEmpty() || content.contains("[Forwarded_Message]")) {
                binding.clMessage.visibility = View.GONE
                binding.checkbox.visibility = View.GONE
                applyTheme(theme)
                return
            }
        }
        binding.clMessage.visibility = View.VISIBLE

        if (isEnabledAITranslate && message is AITranslateIncomingChatMessageEntity) {
            updateTranslatedContent(message)
        } else {
            binding.tvMessage.text = message?.getContent()
        }

        binding.tvMessage.setTextColor(theme.getMainTextColor())

        binding.tvTime.text = message?.getTime()?.convertToStringTime()

        val avatarHolder = ContextCompat.getDrawable(binding.root.context, R.drawable.user_avatar_holder)
        binding.ivAvatar.setImageDrawable(avatarHolder)

        val sender = message?.getSender()
        val isAvatarExist = !sender?.getAvatarUrl().isNullOrEmpty()
        if (isAvatarExist) {
            binding.ivAvatar.loadCircleImageFromUrl(sender?.getAvatarUrl(), R.drawable.user_avatar_holder)
        }

        setTextListener(message, textListener)

        if (isForwardState) {
            binding.checkbox.isChecked = false
            binding.checkbox.visibility = View.VISIBLE

            if (selectedMessages.isNotEmpty()) {
                val foundMessage = selectedMessages.get(0)

                if (foundMessage is ForwardedRepliedMessageEntity && message?.getMessageId() != null
                    && message.getMessageId() == foundMessage.getMessageId() && foundMessage.getRelatedMessageId()
                        .isNullOrEmpty()
                ) {
                    binding.checkbox.isChecked = true
                }
            }

            binding.checkbox.setOnClickListener {
                val isChecked = binding.checkbox.isChecked
                if (isChecked) {
                    binding.checkbox.isChecked = true
                    checkBoxListener?.onSelected(message)
                } else {
                    binding.checkbox.isChecked = false
                    checkBoxListener?.onUnselected(message)
                }
            }
        } else {
            setAIListener(message, aiListener)
            setTranslateListener(message, aiListener)
        }

        binding.tvName.text = sender?.getName() ?: sender?.getLogin()
        binding.progressAI.visibility = View.GONE

        applyTheme(theme)
    }

    private fun showForwardedReplyMessages(
        message: ForwardedRepliedMessageEntity,
        listener: MessageListener?,
        theme: UiKitTheme,
        isForwardState: Boolean,
        aiListener: AIListener?,
    ) {
        if (message.getForwardedRepliedMessages()?.isEmpty() == true) {
            return
        }
        val forwardReplyView = buildIncomingMessage(
            message as IncomingChatMessageEntity,
            listener,
            theme,
            isForwardState,
            aiListener
        )
        binding.flForwardReplyContainer.addView(forwardReplyView)
    }

    private fun updateTranslatedContent(message: AITranslateIncomingChatMessageEntity) {
        val context = binding.tvAITranslate.context
        if (message.isTranslated() == true) {
            binding.tvMessage.text = message.getTranslation()
            binding.tvAITranslate.text = context.getString(R.string.show_original)
        } else {
            binding.tvMessage.text = message.getContent()
            binding.tvAITranslate.text = context.getString(R.string.show_translate)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTextListener(message: ForwardedRepliedMessageEntity?, listener: MessageListener?) {
        binding.tvMessage.setOnClickListener {
            listener?.onClick(message as ForwardedRepliedMessageEntity)
        }
        binding.tvMessage.setOnLongClickListener {
            true
        }
        binding.tvMessage.setOnTouchListener(
            TouchListener(binding.tvMessage.context, message, listener, binding.tvMessage)
        )
    }

    private fun setAIListener(message: ForwardedRepliedMessageEntity?, listener: AIListener?) {
        binding.ivAI.setOnClickListener {
            listener?.onIconClick(message)
        }
    }

    private fun setTranslateListener(message: ForwardedRepliedMessageEntity?, listener: AIListener?) {
        binding.tvAITranslate.setOnClickListener {
            listener?.onTranslateClick(message)

            if (message is AITranslateIncomingChatMessageEntity) {
                return@setOnClickListener
            }

            binding.progressAI.visibility = View.VISIBLE

            if (isEnabledAIAnswerAssistant) {
                binding.ivAI.visibility = View.GONE
            }
        }
    }

    private fun applyTheme(theme: UiKitTheme) {
        setBackgroundMessageColor(theme.getIncomingMessageColor())
        setNameColor(theme.getTertiaryElementsColor())
        setTimeTextColor(theme.getTertiaryElementsColor())
        binding.tvAITranslate.setTextColor(theme.getTertiaryElementsColor())
        binding.progressAI.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())

        binding.ivAI.setColorFilter(theme.getMainElementsColor())
        setCheckBoxColor(theme.getMainElementsColor())
    }

    fun setCheckBoxColor(@ColorInt color: Int) {
        val states: Array<IntArray> = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val defaultColor = ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
        val colors = intArrayOf(color, defaultColor)
        binding.checkbox.buttonTintList = ColorStateList(states, colors)
    }

    fun setBackgroundMessageColor(@ColorInt color: Int) {
        binding.tvMessage.setBackgroundTintList(ColorStateList.valueOf(color))
    }

    fun setNameColor(@ColorInt color: Int) {
        binding.tvName.setTextColor(color)
    }

    fun setTimeTextColor(@ColorInt color: Int) {
        binding.tvTime.setTextColor(color)
    }

    fun setShowAIIcon(show: Boolean) {
        isEnabledAIAnswerAssistant = show

        if (show) {
            binding.ivAI.visibility = View.VISIBLE
        } else {
            binding.ivAI.visibility = View.GONE
        }
    }

    override fun setChecked(checked: Boolean, selectedMessages: MutableList<MessageEntity>) {
        if (message?.isForwardedOrReplied() == true) {
            if (selectedMessages.contains(message as MessageEntity)) {
                binding.checkbox.isChecked = checked
            } else {
                getCheckbox()?.isChecked = checked
            }
        } else {
            binding.checkbox.isChecked = checked
        }
        if (!checked) {
            checkBoxListener?.onUnselected(message)
        }
    }

    fun setShowAITranslate(show: Boolean) {
        isEnabledAITranslate = show

        if (show) {
            binding.tvAITranslate.visibility = View.VISIBLE
        } else {
            binding.tvAITranslate.visibility = View.GONE
        }
    }

    override fun setCheckBoxListener(checkBoxListener: MessageAdapter.CheckBoxListener) {
        super.setCheckBoxListener(checkBoxListener)
        this.checkBoxListener = checkBoxListener
    }

    inner class TouchListener(
        context: Context,
        private val message: ForwardedRepliedMessageEntity?,
        private val listener: MessageListener?,
        private val view: View,
    ) : View.OnTouchListener {
        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, GestureListener())
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val x = e.rawX.toInt()
                val y = e.rawY.toInt()
                listener?.onLongClick(message, adapterPosition, view, x, y)
            }
        }
    }
}