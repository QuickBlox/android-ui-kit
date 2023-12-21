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
import com.quickblox.android_ui_kit.databinding.PositiveNegativeDialogLayoutBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class PositiveNegativeDialog private constructor(
    private val context: Context,
    private val text: String,
    private val positiveText: String,
    private val negativeText: String,
    private val themeDialog: UiKitTheme?,
    private val positiveListener: (() -> Unit)? = null,
    private val negativeListener: (() -> Unit)? = null,
    private val canceledOnTouchOutside: Boolean = true,
) : Dialog(context, R.style.RoundedCornersDialog) {
    companion object {
        fun show(
            context: Context,
            text: String,
            positiveText: String,
            negativeText: String,
            themeDialog: UiKitTheme?,
            positiveListener: (() -> Unit)? = null,
            negativeListener: (() -> Unit)? = null,
            canceledOnTouchOutside: Boolean = true,
        ) {
            PositiveNegativeDialog(
                context,
                text,
                positiveText,
                negativeText,
                themeDialog,
                positiveListener,
                negativeListener,
                canceledOnTouchOutside
            ).show()
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        val binding = PositiveNegativeDialogLayoutBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        applyTheme(binding)
        applyParams()

        binding.tvText.text = text
        binding.tvPositive.text = positiveText
        binding.tvNegative.text = negativeText

        setClickListeners(binding)
        setCanceledOnTouchOutside(canceledOnTouchOutside)
    }

    private fun applyParams() {
        setCancelable(true)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun applyTheme(binding: PositiveNegativeDialogLayoutBinding) {
        themeDialog?.getMainElementsColor()?.let {
            binding.tvPositive.setTextColor(it)
            binding.tvPositive.makeClickableBackground(it)
            binding.tvNegative.setTextColor(it)
            binding.tvNegative.makeClickableBackground(it)
        }

        themeDialog?.getMainTextColor()?.let {
            binding.tvText.setTextColor(it)
        }

        themeDialog?.getMainBackgroundColor()?.let {
            binding.root.setBackgroundColor(it)
        }
    }

    private fun setClickListeners(binding: PositiveNegativeDialogLayoutBinding) {
        binding.tvNegative.setOnClick {
            dismiss()
            negativeListener?.invoke()
        }

        binding.tvPositive.setOnClick {
            dismiss()
            positiveListener?.invoke()
        }
    }
}