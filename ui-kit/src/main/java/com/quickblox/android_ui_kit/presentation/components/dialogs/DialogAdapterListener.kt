/*
 * Created by Injoit on 8.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.dialogs

import com.quickblox.android_ui_kit.domain.entity.DialogEntity

interface DialogAdapterListener {
    fun onClick(dialog: DialogEntity)
    fun onLongClick(dialog: DialogEntity)
}