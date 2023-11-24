/*
 * Created by Injoit on 29.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.send

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.chip.Chip
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.SendMessageComponentBinding
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent.MessageComponentStates
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent.MessageComponentStates.*
import com.quickblox.android_ui_kit.presentation.dialogs.AttachmentsDialog
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.SimpleTextWatcher
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class SendMessageComponentImpl : ConstraintLayout, SendMessageComponent {
    private var binding: SendMessageComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()
    private var componentState: MessageComponentStates = VOICE_MESSAGE
    private var listener: SendMessageComponentListener? = null
    private var textWatcher: TextWatcher? = null
    private var typingTimer = TypingTimer()
    private var enabledAIRephrase: Boolean = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val rootView: View = inflate(context, R.layout.send_message_component, this)
        binding = SendMessageComponentBinding.bind(rootView)

        setListeners(context)
        setDefaultState()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(context: Context) {
        binding?.ivSend?.setOnClick {
            val textMessage = binding?.etMessage?.text.toString()
            if (componentState == FORWARDING_MESSAGE) {
                listener?.onSendTextMessageClickListener(textMessage)
                if (textMessage.isNotEmpty()) {
                    binding?.etMessage?.text?.clear()
                }
            } else {
                if (textMessage.isNotEmpty()) {
                    listener?.onSendTextMessageClickListener(textMessage)
                    binding?.etMessage?.text?.clear()

                    typingTimer.stop()
                    listener?.onStoppedTyping()
                }
            }
        }

        binding?.ivAttachment?.setOnClick {
            AttachmentsDialog.show(
                context, context.getString(R.string.send_attachment), theme, AttachmentsDialogListenerImpl()
            )
        }

        binding?.ivSendVoice?.setOnTouchListener(object : OnTouchListener {
            val handler = Handler(Looper.getMainLooper())
            var isChronometerStarted = false
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val alpha = 45
                        val colorWithAlpha = setAlphaToColor(alpha, theme.getMainElementsColor())
                        view.setBackgroundColor(colorWithAlpha)

                        handler.postDelayed({
                            startRecordVoice()
                            isChronometerStarted = true
                            listener?.onStartRecordVoiceClickListener()
                        }, 1000L)
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.setBackgroundColor(Color.TRANSPARENT)

                        handler.removeCallbacksAndMessages(null)

                        if (isChronometerStarted) {
                            listener?.onStopRecordVoiceClickListener()
                            stopRecordVoice()
                            isChronometerStarted = false
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setAlphaToColor(alpha: Int, color: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun setDefaultState() {
        setDefaultTextWatcher()
        applyTheme()
    }

    private fun setDefaultTextWatcher() {
        val textWatcher = buildDefaultTextWatcher()
        setTextWatcherToEditText(textWatcher)
    }

    private fun buildDefaultTextWatcher(): TextWatcher {
        return object : SimpleTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val text = charSequence.toString()
                listener?.onChangedRephraseText()
                if (text.isEmpty()) {
                    setVoiceState()
                    showRephraseTones(false)
                    listener?.onClearRephraseOriginalText()
                    return
                }

                showRephraseTones(true)
                setChatState()
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().isEmpty()) {
                    return
                }

                if (typingTimer.isNotRunning()) {
                    listener?.onStartedTyping()
                }

                val typingDelay = 1000L
                typingTimer.start(typingDelay) {
                    listener?.onStoppedTyping()
                }
            }
        }
    }

    private fun buildAnimationToRecording(): AlphaAnimation {
        val animation = AlphaAnimation(0f, 1f)
        animation.duration = 700
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = Animation.INFINITE
        return animation
    }

    private fun setChatState() {
        if (componentState != FORWARDING_MESSAGE) {
            componentState = CHAT_MESSAGE
        }
        binding?.ivSendVoice?.visibility = View.GONE
        binding?.ivSend?.visibility = View.VISIBLE
    }

    private fun setVoiceState() {
        if (componentState == FORWARDING_MESSAGE) {
            return
        }
        componentState = VOICE_MESSAGE
        binding?.ivSendVoice?.visibility = View.VISIBLE
        binding?.ivSend?.visibility = View.GONE
    }

    private fun startRecordVoice() {
        binding?.etMessage?.hint = ""
        binding?.llRecordVoice?.visibility = View.VISIBLE
        binding?.chronometer?.base = SystemClock.elapsedRealtime();
        binding?.chronometer?.start()

        val animation = buildAnimationToRecording()
        binding?.ivRecord?.startAnimation(animation)
    }

    private fun stopRecordVoice() {
        binding?.llRecordVoice?.visibility = View.GONE
        binding?.etMessage?.hint = binding?.root?.context?.getString(R.string.type_message)
        binding?.chronometer?.stop()

        binding?.ivRecord?.clearAnimation()
    }

    @SuppressLint("RestrictedApi")
    private fun applyTheme() {
        binding?.etMessage?.supportBackgroundTintList = ColorStateList.valueOf(theme.getInputBackgroundColor())
        setMessageTextColor(theme.getMainTextColor())
        setMessageHintColor(theme.getSecondaryTextColor())
        setBackground(theme.getMainBackgroundColor())

        setTimerTextColor(theme.getMainTextColor())
        setVoiceButtonColor(theme.getSecondaryElementsColor())
        setEmojiButtonColor(theme.getSecondaryElementsColor())
        setAttachmentButtonColor(theme.getSecondaryElementsColor())
        setSendMessageButtonColor(theme.getMainElementsColor())

        binding?.ivRecord?.setColorFilter(theme.getErrorColor())
        binding?.tvTyping?.setTextColor(theme.getTertiaryElementsColor())

        binding?.ivSendVoice?.makeClickableBackground(theme.getMainElementsColor())
        binding?.ivSend?.makeClickableBackground(theme.getMainElementsColor())
        binding?.ivAttachment?.makeClickableBackground(theme.getMainElementsColor())
        binding?.vDivider?.setBackgroundColor(theme.getDividerColor())
    }

    private fun setTimerTextColor(color: Int) {
        binding?.chronometer?.setTextColor(color)
    }

    override fun getComponentMessageState(): MessageComponentStates {
        return componentState
    }

    override fun setComponentMessageState(state: MessageComponentStates) {
        componentState = state
        when (state) {
            CHAT_MESSAGE -> {
                setChatState()
            }

            VOICE_MESSAGE -> {
                setVoiceState()
            }
            FORWARDING_MESSAGE -> {
                setChatState()
            }
        }
    }

    override fun getMessageHint(): String {
        return binding?.etMessage?.hint.toString()
    }

    override fun setMessageHint(text: String?) {
        binding?.etMessage?.hint = text
    }

    override fun setMessageHintColor(color: Int) {
        binding?.etMessage?.setHintTextColor(color)
    }

    override fun getMessageText(): String {
        return binding?.etMessage?.text.toString()
    }

    override fun setMessageText(text: String?) {
        binding?.etMessage?.setText(text)
    }

    override fun setTextWatcherToEditText(textWatcher: TextWatcher?) {
        if (textWatcher == null) {
            binding?.etMessage?.removeTextChangedListener(this.textWatcher)
        } else {
            binding?.etMessage?.addTextChangedListener(textWatcher)
            this.textWatcher = textWatcher
        }
    }

    override fun setMessageTextColor(color: Int) {
        binding?.etMessage?.setTextColor(color)
    }

    override fun getSendMessageComponentListener(): SendMessageComponentListener? {
        return listener
    }

    override fun getMessageEditText(): AppCompatEditText? {
        return binding?.etMessage
    }

    override fun getAttachmentContainer(): FrameLayout? {
        return binding?.flForwardReplyContainer
    }

    override fun enableRephrase(enable: Boolean) {
        enabledAIRephrase = enable
    }

    override fun setSendMessageComponentListener(listener: SendMessageComponentListener?) {
        this.listener = listener
    }

    override fun setAttachmentButtonColor(color: Int) {
        binding?.ivAttachment?.setColorFilter(color)
    }

    override fun setImageAttachmentButton(resource: Int) {
        binding?.ivAttachment?.setImageResource(resource)
    }

    override fun setEmojiButtonColor(color: Int) {
        binding?.ivEmoji?.setColorFilter(color)
    }

    override fun setImageEmojiButton(resource: Int) {
        binding?.ivEmoji?.setImageResource(resource)
    }

    override fun setSendMessageButtonColor(color: Int) {
        binding?.ivSend?.setColorFilter(color)
    }

    override fun setImageSendMessageButton(resource: Int) {
        binding?.ivSend?.setImageResource(resource)
    }

    override fun setVoiceButtonColor(color: Int) {
        binding?.ivSendVoice?.setColorFilter(color)
    }

    override fun setImageVoiceButton(resource: Int) {
        binding?.ivSendVoice?.setImageResource(resource)
    }

    override fun showStartedTyping(text: String) {
        binding?.tvTyping?.text = text
        binding?.tvTyping?.visibility = View.VISIBLE
    }

    override fun showStoppedTyping() {
        binding?.tvTyping?.visibility = View.GONE
        binding?.tvTyping?.text = ""
    }

    override fun isShowButtonAttachment(show: Boolean) {
        if (show) {
            binding?.ivAttachment?.visibility = View.VISIBLE
        } else {
            binding?.ivAttachment?.visibility = View.GONE
        }
    }

    override fun setBackground(color: Int) {
        binding?.root?.setBackgroundColor(color)
    }

    override fun setRephraseTones(tones: List<AIRephraseEntity>) {
        tones.forEach { tone ->
            val chip = Chip(context)
            chip.text = buildChipText(tone)
            chip.setTextColor(theme.getMainTextColor())
            chip.chipBackgroundColor = ColorStateList.valueOf(theme.getOutgoingMessageColor())
            chip.setOnClickListener {
                listener?.onClickedTone(tone)
            }

            binding?.chipGroup?.addView(chip)
        }
    }

    private fun buildChipText(rephraseEntity: AIRephraseEntity): String {
        val stringBuilder = StringBuilder()

        val icon = rephraseEntity.getRephraseTone().getIcon()
        if (icon.isNotBlank()) {
            stringBuilder.append(icon)
            stringBuilder.append(" ")
        }

        stringBuilder.append(rephraseEntity.getRephraseTone().getName())
        val isNeedAddTone = !rephraseEntity.getRephraseTone().getName().contains("Back to original text")
        if (isNeedAddTone) {
            stringBuilder.append(" ")
            stringBuilder.append(resources.getString(R.string.tone))
        }

        return stringBuilder.toString()
    }

    override fun showRephraseTones(show: Boolean) {
        val isNotEnabledAIRephrase = !enabledAIRephrase
        if (isNotEnabledAIRephrase) {
            return
        }
        if (show) {
            binding?.horizontalScrollView?.visibility = View.VISIBLE
        } else {
            binding?.horizontalScrollView?.visibility = View.GONE
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }

    interface SendMessageComponentListener {
        fun onSendTextMessageClickListener(textMessage: String)
        fun onStartRecordVoiceClickListener()
        fun onStopRecordVoiceClickListener()

        fun onClickPhotoCamera()
        fun onClickVideoCamera()
        fun onClickPhotoAndVideoGallery()
        fun onClickFile()

        fun onStartedTyping()
        fun onStoppedTyping()

        fun onClickedTone(rephraseEntity: AIRephraseEntity)
        fun onClearRephraseOriginalText()
        fun onChangedRephraseText()
    }

    private inner class AttachmentsDialogListenerImpl : AttachmentsDialog.AttachmentsDialogListener {
        override fun onClickPhotoCamera() {
            listener?.onClickPhotoCamera()
        }

        override fun onClickVideoCamera() {
            listener?.onClickVideoCamera()
        }

        override fun onClickPhotoAndVideoGallery() {
            listener?.onClickPhotoAndVideoGallery()
        }

        override fun onClickFile() {
            listener?.onClickFile()
        }
    }
}