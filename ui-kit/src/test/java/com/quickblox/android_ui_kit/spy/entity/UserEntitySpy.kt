/*
 * Created by Injoit on 18.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.spy.entity

import com.quickblox.android_ui_kit.stub.entity.UserEntityStub
import kotlin.random.Random

open class UserEntitySpy : UserEntityStub() {
    private var userId = Random.nextInt(1000, 10000)

    override fun getUserId(): Int {
        return userId
    }

    override fun getLogin(): String? {
        return "UserLogin: ${System.currentTimeMillis()}"
    }

    override fun getName(): String? {
        return "UserName: ${System.currentTimeMillis()}"
    }
}