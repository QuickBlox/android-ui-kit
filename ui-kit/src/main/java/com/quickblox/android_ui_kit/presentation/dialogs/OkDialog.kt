/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.view.Window
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.OkDialogLayoutBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class OkDialog private constructor(
    private val context: Context,
    private val text: String,
    private val themeDialog: UiKitTheme?
) : Dialog(context, R.style.RoundedCornersDialog) {
    companion object {
        fun show(context: Context, text: String, themeDialog: UiKitTheme?) {
            OkDialog(context, text, themeDialog).show()
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        val binding = OkDialogLayoutBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        applyTheme(binding)
        applyParams()

        binding.tvText.text = text
        binding.tvOK.text = context.getString(R.string.ok)

        binding.tvOK.setOnClick {
            dismiss()
        }
    }

    private fun applyParams() {
        setCancelable(true)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun applyTheme(binding: OkDialogLayoutBinding) {
        themeDialog?.getMainElementsColor()?.let {
            binding.tvOK.setTextColor(it)
            binding.tvOK.makeClickableBackground(it)
        }

        themeDialog?.getMainTextColor()?.let {
            binding.tvOK.setTextColor(it)
        }

        themeDialog?.getMainBackgroundColor()?.let {
            binding.tvOK.setBackgroundColor(it)
        }
    }
}