/*
 * Created by Injoit on 26.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders.factory

import android.view.ViewGroup
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*

interface MessageViewHolderFactory {
    fun createMessageViewHolder(code: Int, parent: ViewGroup): BaseViewHolder<*>
    fun createDateHeaderViewHolder(parent: ViewGroup): DateHeaderViewHolder
    fun createEventViewHolder(parent: ViewGroup): EventViewHolder
    fun createTextIncomingViewHolder(parent: ViewGroup): TextIncomingViewHolder
    fun createTextOutgoingViewHolder(parent: ViewGroup): TextOutgoingViewHolder
    fun createImageIncomingViewHolder(parent: ViewGroup): ImageIncomingViewHolder
    fun createImageOutgoingViewHolder(parent: ViewGroup): ImageOutgoingViewHolder
    fun createAudioIncomingViewHolder(parent: ViewGroup): AudioIncomingViewHolder
    fun createAudioOutgoingViewHolder(parent: ViewGroup): AudioOutgoingViewHolder
    fun createVideoIncomingViewHolder(parent: ViewGroup): VideoIncomingViewHolder
    fun createVideoOutgoingViewHolder(parent: ViewGroup): VideoOutgoingViewHolder
    fun createFileIncomingViewHolder(parent: ViewGroup): FileIncomingViewHolder
    fun createFileOutgoingViewHolder(parent: ViewGroup): FileOutgoingViewHolder
}