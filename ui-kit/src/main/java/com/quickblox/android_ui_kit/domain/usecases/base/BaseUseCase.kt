/*
 * Created by Injoit on 7.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases.base

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

@ExcludeFromCoverage
abstract class BaseUseCase<Result> : UseCase<Result> {
    protected fun isScopeNotActive(scope: CoroutineScope): Boolean {
        return !scope.isActive
    }

    override suspend fun release() {}
}