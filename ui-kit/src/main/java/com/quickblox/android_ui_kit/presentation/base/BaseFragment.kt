/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.base

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quickblox.android_ui_kit.presentation.screens.UIKitScreen

abstract class BaseFragment : Fragment(), UIKitScreen {
    abstract fun collectViewsTemplateMethod(context: Context): List<View?>

    protected fun showToast(title: String) {
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
    }
}