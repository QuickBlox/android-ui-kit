/*
 * Created by Injoit on 24.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.usecases.base

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import kotlinx.coroutines.flow.Flow

@ExcludeFromCoverage
abstract class FlowUseCase<TResult> : BaseUseCase<Flow<TResult>>() {
}