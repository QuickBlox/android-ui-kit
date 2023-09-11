/*
 * Created by Injoit on 29.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.send

import android.text.TextWatcher
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatEditText
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentImpl.SendMessageComponentListener

interface SendMessageComponent : Component {
    enum class MessageComponentStates { VOICE_MESSAGE, CHAT_MESSAGE }

    fun getComponentMessageState(): MessageComponentStates
    fun setComponentMessageState(state: MessageComponentStates)

    fun getMessageHint(): String
    fun setMessageHint(text: String?)
    fun setMessageHintColor(@ColorInt color: Int)

    fun getMessageText(): String
    fun setMessageText(text: String?)
    fun setTextWatcherToEditText(textWatcher: TextWatcher?)
    fun setMessageTextColor(@ColorInt color: Int)

    fun getSendMessageComponentListener(): SendMessageComponentListener?
    fun setSendMessageComponentListener(listener: SendMessageComponentListener?)

    fun setAttachmentButtonColor(@ColorInt color: Int)
    fun setImageAttachmentButton(@DrawableRes resource: Int)

    fun setEmojiButtonColor(@ColorInt color: Int)
    fun setImageEmojiButton(@DrawableRes resource: Int)

    fun setSendMessageButtonColor(@ColorInt color: Int)
    fun setImageSendMessageButton(@DrawableRes resource: Int)

    fun setVoiceButtonColor(@ColorInt color: Int)
    fun setImageVoiceButton(@DrawableRes resource: Int)

    fun showStartedTyping(text: String)
    fun showStoppedTyping()

    fun setBackground(@ColorInt color: Int)
    fun getMessageEditText(): AppCompatEditText?

    fun setRephraseTones(tones: List<AIRephraseToneEntity>)
    fun showRephraseTones(show: Boolean)
}