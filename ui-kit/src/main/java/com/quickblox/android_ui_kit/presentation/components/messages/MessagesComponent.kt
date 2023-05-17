/*
 * Created by Injoit on 21.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages

import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.AudioIncomingViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.AudioIncomingViewHolder.AudioIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.AudioOutgoingViewHolder.AudioOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.FileIncomingViewHolder.FileIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.FileOutgoingViewHolder.FileOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageIncomingViewHolder.ImageIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.ImageOutgoingViewHolder.ImageOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.TextIncomingViewHolder.TextIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.TextOutgoingViewHolder.TextOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.VideoIncomingViewHolder.VideoIncomingListener
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.VideoOutgoingViewHolder.VideoOutgoingListener
import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponent

interface MessagesComponent : Component {
    fun getSendMessageComponent(): SendMessageComponent?

    fun setAdapter(adapter: MessageAdapter?)
    fun getAdapter(): MessageAdapter?

    fun setImageIncomingListener(listener: ImageIncomingListener?)
    fun getImageIncomingListener(): ImageIncomingListener?

    fun setImageOutgoingListener(listener: ImageOutgoingListener?)
    fun getImageOutgoingListener(): ImageOutgoingListener?

    fun setTextIncomingListener(listener: TextIncomingListener?)
    fun getTextIncomingListener(): TextIncomingListener?

    fun setTextOutgoingListener(listener: TextOutgoingListener?)
    fun getTextOutgoingListener(): TextOutgoingListener?

    fun setVideoOutgoingListener(listener: VideoOutgoingListener?)
    fun getVideoOutgoingListener(): VideoOutgoingListener?

    fun setVideoIncomingListener(listener: VideoIncomingListener?)
    fun getVideoIncomingListener(): VideoIncomingListener?

    fun setFileOutgoingListener(listener: FileOutgoingListener?)
    fun getFileOutgoingListener(): FileOutgoingListener?

    fun setFileIncomingListener(listener: FileIncomingListener?)
    fun getFileIncomingListener(): FileIncomingListener?

    fun setAudioOutgoingListener(listener: AudioOutgoingListener?)
    fun getAudioOutgoingListener(): AudioOutgoingListener?

    fun setAudioIncomingListener(listener: AudioIncomingListener?)
    fun getAudioIncomingListener(): AudioIncomingListener?

    fun setOnScrollListener(listener: RecyclerView.OnScrollListener)
    fun scrollToDown()

    fun showProgress()
    fun hideProgress()
}