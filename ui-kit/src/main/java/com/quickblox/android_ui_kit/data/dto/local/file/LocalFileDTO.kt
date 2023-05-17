/*
 * Created by Injoit on 02.02.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.data.dto.local.file

import android.net.Uri
import java.io.File

class LocalFileDTO {
    var uri: Uri? = null
    var url: String? = null
    var file: File? = null
    var name: String? = null
    var id: String? = null
    var type: String? = null
    var data: ByteArray? = null
    var mimeType: String? = null
}