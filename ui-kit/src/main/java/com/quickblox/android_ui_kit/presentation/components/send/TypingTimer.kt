/*
 * Created by Injoit on 1.6.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.send

import java.util.*

class TypingTimer {
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var isNotRunning = true

     fun start(delay: Long, finishCallback: () -> Unit) {
        cancel()

        timer = Timer()
        timerTask = createTimerTaskWith(finishCallback)

        isNotRunning = false
        timer?.schedule(timerTask, delay)
    }

    private fun cancel() {
        timer?.cancel()
        timerTask?.cancel()
    }

     fun isNotRunning(): Boolean {
        return isNotRunning
    }

     fun stop() {
        cancel()

        isNotRunning = true
    }

    private fun createTimerTaskWith(finishCallback: () -> Unit): TimerTask {
        return object : TimerTask() {
            override fun run() {
                finishCallback.invoke()
                isNotRunning = true
            }
        }
    }
}