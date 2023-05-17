/*
 * Created by Injoit on 4.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.lifecycle

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.ConnectionUseCase
import com.quickblox.android_ui_kit.domain.usecases.SyncDialogsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExcludeFromCoverage
object AppLifecycleManager {
    private val TAG = AppLifecycleManager::class.java.simpleName

    private val scope = CoroutineScope(Dispatchers.Main)
    private val syncUseCase: SyncDialogsUseCase = SyncDialogsUseCase()
    private val connectionUseCase: ConnectionUseCase = ConnectionUseCase()
    private val lifecycleEventObserver = LifecycleEventObserverImpl()

    fun init() {
        releaseUseCases()

        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleEventObserver)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    private fun initUseCases() {
        scope.launch {
            try {
                connectionUseCase.execute()
                syncUseCase.execute()
            } catch (exception: DomainException) {
                Log.e(TAG, exception.message.toString())
            }
        }
    }

    private fun releaseUseCases() {
        scope.launch {
            connectionUseCase.release()
            syncUseCase.release()
        }
    }

    private class LifecycleEventObserverImpl : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == ON_START) {
                initUseCases()
            }
            if (event == ON_STOP) {
                releaseUseCases()
            }
        }
    }
}