/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages

import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.AIListener
import com.quickblox.android_ui_kit.presentation.base.BaseMessageViewHolder.MessageListener
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent

interface MessagesComponent : Component {
    fun getSendMessageComponent(): SendMessageComponent?

    fun setAdapter(adapter: MessageAdapter?)
    fun getAdapter(): MessageAdapter?

    fun setImageIncomingListener(listener: MessageListener?)
    fun getImageIncomingListener(): MessageListener?

    fun setImageOutgoingListener(listener: MessageListener?)
    fun getImageOutgoingListener(): MessageListener?

    fun setTextIncomingListener(listener: MessageListener?)
    fun getTextIncomingListener(): MessageListener?

    fun setAIListener(aiListener: AIListener)
    fun getAIListener(): AIListener?

    fun enableAI()
    fun disableAI()

    fun setTextOutgoingListener(listener: MessageListener?)
    fun getTextOutgoingListener(): MessageListener?

    fun setVideoOutgoingListener(listener: MessageListener?)
    fun getVideoOutgoingListener(): MessageListener?

    fun setVideoIncomingListener(listener: MessageListener?)
    fun getVideoIncomingListener(): MessageListener?

    fun setFileOutgoingListener(listener: MessageListener?)
    fun getFileOutgoingListener(): MessageListener?

    fun setFileIncomingListener(listener: MessageListener?)
    fun getFileIncomingListener(): MessageListener?

    fun setAudioOutgoingListener(listener: MessageListener?)
    fun getAudioOutgoingListener(): MessageListener?

    fun setAudioIncomingListener(listener: MessageListener?)
    fun getAudioIncomingListener(): MessageListener?

    fun setOnScrollListener(listener: RecyclerView.OnScrollListener)
    fun scrollToDown()
    fun scrollToPosition(position: Int)

    fun showProgress()
    fun hideProgress()
}