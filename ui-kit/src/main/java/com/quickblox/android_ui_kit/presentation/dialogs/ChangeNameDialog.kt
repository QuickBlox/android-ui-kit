/*
 * Created by Injoit on 14.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.ChangeNameDialogLayoutBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class ChangeNameDialog private constructor(
    context: Context, private val themeDialog: UiKitTheme?, private val listener: ((name: String) -> Unit),
) : Dialog(context, R.style.RoundedCornersDialog) {
    companion object {
        fun show(context: Context, themeDialog: UiKitTheme?, listener: ((name: String) -> Unit)) {
            ChangeNameDialog(context, themeDialog, listener).show()
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        val binding = ChangeNameDialogLayoutBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        applyParams()
        applyTheme(binding)

        setClickListeners(binding)
    }

    private fun applyTheme(binding: ChangeNameDialogLayoutBinding) {
        themeDialog?.getSecondaryTextColor()?.let {
            binding.etChangeName.setHintTextColor(it)
        }

        themeDialog?.getMainElementsColor()?.let {
            binding.etChangeName.backgroundTintList = ColorStateList.valueOf(it)
            binding.tvOk.setTextColor(it)
            binding.tvOk.makeClickableBackground(it)
            binding.tvCancel.setTextColor(it)
            binding.tvCancel.makeClickableBackground(it)
        }

        themeDialog?.getMainTextColor()?.let {
            binding.tvTitle.setTextColor(it)
            binding.etChangeName.setTextColor(it)
        }

        themeDialog?.getMainBackgroundColor()?.let {
            binding.root.setBackgroundColor(it)
        }
    }

    private fun setClickListeners(binding: ChangeNameDialogLayoutBinding) {
        binding.tvCancel.setOnClick {
            dismiss()
        }

        binding.tvOk.setOnClick {
            val name = binding.etChangeName.text.toString()
            val minimumCharactersLength = 3
            if (name.length >= minimumCharactersLength) {
                dismiss()
                listener.invoke(name)
            } else {
                Toast.makeText(context, "Minimum number of characters 3", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyParams() {
        setCancelable(true)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}