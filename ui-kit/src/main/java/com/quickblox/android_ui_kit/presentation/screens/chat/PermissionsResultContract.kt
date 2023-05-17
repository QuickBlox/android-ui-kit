/*
 * Created by Injoit on 10.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSION_GRANT_RESULTS

val EXTRA_DATA = "extra_data"

class PermissionsContract : ActivityResultContract<Intent, Pair<Boolean, String?>>() {
    private var extraData: String? = null

    override fun createIntent(context: Context, input: Intent): Intent {
        extraData = input.getStringExtra(EXTRA_DATA) ?: ""
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, String?> {
        if (intent == null || resultCode != Activity.RESULT_OK) {
            return Pair(false, extraData)
        }
        val grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
        val isGranted = grantResults?.any { result ->
            result == PackageManager.PERMISSION_GRANTED
        } == true

        return Pair(isGranted, extraData)
    }
}