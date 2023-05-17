/*
 * Created by Injoit on 29.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.send

import com.quickblox.android_ui_kit.presentation.components.send.SendMessageComponentImpl.SendMessageComponentListener

open class SendMessageComponentListenerImpl : SendMessageComponentListener {
    override fun onSendTextMessageClickListener(textMessage: String) {}
    override fun onStartRecordVoiceClickListener() {}
    override fun onStopRecordVoiceClickListener() {}

    override fun onClickPhotoCamera() {}
    override fun onClickVideoCamera() {}
    override fun onClickPhotoAndVideoGallery() {}
    override fun onClickFile() {}
}