/*
 * Created by Injoit on 13.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.send

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object Recorder {
    private var mediaRecorder: MediaRecorder? = null
    var uri: Uri? = null

    suspend fun startRecording(context: Context, file: File?) {
        withContext(Dispatchers.IO) {
            stopRecording()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = MediaRecorder(context)
            } else {
                mediaRecorder = MediaRecorder()
            }

            uri = getUri(file, context)

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(file?.absolutePath)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        }
    }

    suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            mediaRecorder?.stop()
            mediaRecorder?.release()

            mediaRecorder = null
        }
    }

    private fun getUri(file: File?, context: Context): Uri? {
        return file?.let {
            FileProvider.getUriForFile(context, context.packageName + ".provider", it)
        }
    }
}