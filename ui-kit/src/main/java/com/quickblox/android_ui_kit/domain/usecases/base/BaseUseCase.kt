/*
 * Created by Injoit on 7.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

abstract class BaseUseCase<Result> : UseCase<Result> {
    override suspend fun release() {}

    protected fun isScopeNotActive(scope: CoroutineScope): Boolean {
        return !scope.isActive
    }
}