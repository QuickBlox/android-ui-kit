/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.remote.user

import java.util.*

class RemoteUserDTO {
    var id: Int? = null
    var name: String? = null
    var email: String? = null
    var login: String? = null
    var phone: String? = null
    var website: String? = null
    var lastRequestAt: Date? = null
    var externalId: String? = null
    var facebookId: String? = null
    var blobId: Int? = null
    var avatarUrl: String? = null
    var tags: String? = null
    var customData: String? = null
}