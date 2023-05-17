/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.domain.usecases.base

interface UseCase<TResult> {
    suspend fun execute(): TResult

    suspend fun release()
}