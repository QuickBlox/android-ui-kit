/*
 * Created by Injoit on 10.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.EXTRA_OUTPUT
import androidx.activity.result.contract.ActivityResultContract
import com.quickblox.android_ui_kit.presentation.screens.parcelable

class CameraResultContract : ActivityResultContract<Intent, Pair<Boolean, Uri?>>() {
    private var uri: Uri? = null

    override fun createIntent(context: Context, input: Intent): Intent {
        uri = input.parcelable(EXTRA_OUTPUT)
        return input
    }

     override fun getSynchronousResult(context: Context, input: Intent):
            SynchronousResult<Pair<Boolean, Uri?>>? = null

    @Suppress("AutoBoxing")
     override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, Uri?> {
        val isSuccess = resultCode == Activity.RESULT_OK
        return Pair(isSuccess, uri)
    }
}