/*
 * Created by Injoit on 21.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

/*
 * Created by Injoit on 21.8.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.domain.entity.implementation

import com.quickblox.android_ui_kit.ExcludeFromCoverage
import com.quickblox.android_ui_kit.domain.entity.AIRephraseEntity
import com.quickblox.android_ui_kit.domain.entity.AIRephraseToneEntity

@ExcludeFromCoverage
class AIRephraseEntityImpl(
    private val tone: AIRephraseToneEntity,
    private val toneType: ToneType = ToneType.NORMAL,
) : AIRephraseEntity {
    enum class ToneType { NORMAL, ORIGINAL }

    private var originalText: String = ""
    private var rephrasedText: String = ""
    override fun getRephraseTone(): AIRephraseToneEntity {
        return tone
    }

    override fun isOriginal(): Boolean {
        return toneType == ToneType.ORIGINAL
    }

    override fun getOriginalText(): String {
        return originalText
    }

    override fun setOriginalText(text: String) {
        originalText = text
    }

    override fun getRephrasedText(): String {
        return rephrasedText
    }

    override fun setRephrasedText(text: String) {
        rephrasedText = text
    }
}