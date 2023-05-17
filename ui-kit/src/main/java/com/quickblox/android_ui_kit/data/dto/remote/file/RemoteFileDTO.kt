/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.remote.file

import android.net.Uri
import java.io.File

class RemoteFileDTO {
    var id: Int? = null
    var uid: String? = null
    var file: File? = null
    var url: String? = null
    var uri: Uri? = null
    var mimeType: String? = null
}