/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit

import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.stub.entity.UserEntityStub

const val DEFAULT_DELAY: Long = 3

open class BaseTest {
    protected fun buildStubXUsers(usersCount: Int): List<UserEntity> {
        val dialogs = mutableListOf<UserEntity>()
        for (index in 1..usersCount) {
            dialogs.add(buildStubUser())
        }
        return dialogs
    }

    protected fun buildStubUser(): UserEntity {
        return UserEntityStub()
    }
}