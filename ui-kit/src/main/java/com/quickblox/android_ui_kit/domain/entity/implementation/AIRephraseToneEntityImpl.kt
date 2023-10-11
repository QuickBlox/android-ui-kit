/*
 * Created by Injoit on 6.10.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity

class AIRephraseToneEntityImpl(
    private val name: String,
    private val description: String,
    private val icon: String = "",
) : AIRephraseToneEntity {
    override fun getName(): String {
       return name
    }

    override fun getDescription(): String {
        return description
    }

    override fun getIcon(): String {
        return icon
    }
}