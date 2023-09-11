/*
 * Created by Injoit on 21.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain.entity

import com.quickblox.android_ui_kit.ExcludeFromCoverage

@ExcludeFromCoverage
interface AIRephraseToneEntity {
    fun getName(): String
    fun getSmileCode(): String
    fun isOriginal(): Boolean
    fun getOriginalText(): String
    fun setOriginalText(text: String)
    fun getRephrasedText(): String
    fun setRephrasedText(text: String)
}