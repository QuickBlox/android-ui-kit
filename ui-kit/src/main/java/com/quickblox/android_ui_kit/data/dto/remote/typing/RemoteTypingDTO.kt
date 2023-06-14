/*
 * Created by Injoit on 25.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.data.dto.remote.typing

class RemoteTypingDTO {
    enum class Types { STARTED, STOPPED }

    var senderId: Int? = null
    var type: Types? = null
}