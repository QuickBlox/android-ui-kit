/*
 * Created by Injoit on 7.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.messages.viewholders.factory

import android.view.ViewGroup
import com.quickblox.android_ui_kit.presentation.base.BaseViewHolder
import com.quickblox.android_ui_kit.presentation.components.messages.MessageAdapter.MessageViewHolderTypes.*
import com.quickblox.android_ui_kit.presentation.components.messages.viewholders.*

class MessageViewHolderFactoryImpl : MessageViewHolderFactory {
    override fun createMessageViewHolder(code: Int, parent: ViewGroup): BaseViewHolder<*> {
        when (code) {
            DATE_HEADER.code -> {
                return createDateHeaderViewHolder(parent)
            }
            EVENT.code -> {
                return createEventViewHolder(parent)
            }
            TEXT_INCOMING.code -> {
                return createTextIncomingViewHolder(parent)
            }
            TEXT_OUTGOING.code -> {
                return createTextOutgoingViewHolder(parent)
            }
            IMAGE_INCOMING.code -> {
                return createImageIncomingViewHolder(parent)
            }
            IMAGE_OUTGOING.code -> {
                return createImageOutgoingViewHolder(parent)
            }
            AUDIO_INCOMING.code -> {
                return createAudioIncomingViewHolder(parent)
            }
            AUDIO_OUTGOING.code -> {
                return createAudioOutgoingViewHolder(parent)
            }
            VIDEO_INCOMING.code -> {
                return createVideoIncomingViewHolder(parent)
            }
            VIDEO_OUTGOING.code -> {
                return createVideoOutgoingViewHolder(parent)
            }
            FILE_INCOMING.code -> {
                return createFileIncomingViewHolder(parent)
            }
            FILE_OUTGOING.code -> {
                return createFileOutgoingViewHolder(parent)
            }
            else -> {
                throw RuntimeException("The type code ($code) of MessageViewHolder doesn't exist ")
            }
        }
    }

    override fun createDateHeaderViewHolder(parent: ViewGroup): DateHeaderViewHolder {
        return DateHeaderViewHolder.newInstance(parent)
    }

    override fun createEventViewHolder(parent: ViewGroup): EventViewHolder {
        return EventViewHolder.newInstance(parent)
    }

    override fun createTextIncomingViewHolder(parent: ViewGroup): TextIncomingViewHolder {
        return TextIncomingViewHolder.newInstance(parent)
    }

    override fun createTextOutgoingViewHolder(parent: ViewGroup): TextOutgoingViewHolder {
        return TextOutgoingViewHolder.newInstance(parent)
    }

    override fun createImageIncomingViewHolder(parent: ViewGroup): ImageIncomingViewHolder {
        return ImageIncomingViewHolder.newInstance(parent)
    }

    override fun createImageOutgoingViewHolder(parent: ViewGroup): ImageOutgoingViewHolder {
        return ImageOutgoingViewHolder.newInstance(parent)
    }

    override fun createAudioIncomingViewHolder(parent: ViewGroup): AudioIncomingViewHolder {
        return AudioIncomingViewHolder.newInstance(parent)
    }

    override fun createAudioOutgoingViewHolder(parent: ViewGroup): AudioOutgoingViewHolder {
        return AudioOutgoingViewHolder.newInstance(parent)
    }

    override fun createVideoIncomingViewHolder(parent: ViewGroup): VideoIncomingViewHolder {
        return VideoIncomingViewHolder.newInstance(parent)
    }

    override fun createVideoOutgoingViewHolder(parent: ViewGroup): VideoOutgoingViewHolder {
        return VideoOutgoingViewHolder.newInstance(parent)
    }

    override fun createFileIncomingViewHolder(parent: ViewGroup): FileIncomingViewHolder {
        return FileIncomingViewHolder.newInstance(parent)
    }

    override fun createFileOutgoingViewHolder(parent: ViewGroup): FileOutgoingViewHolder {
        return FileOutgoingViewHolder.newInstance(parent)
    }
}