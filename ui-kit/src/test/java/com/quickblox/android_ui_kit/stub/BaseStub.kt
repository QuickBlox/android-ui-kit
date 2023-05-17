/*
 * Created by Injoit on 3.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.stub

abstract class BaseStub {
    protected fun buildRuntimeException(): RuntimeException {
        return RuntimeException("expected: override, actual: not override")
    }
}