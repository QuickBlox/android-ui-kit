/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogsCreateBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class CreateDialog constructor(
    context: Context, private val theme: UiKitTheme?, private val listener: CreateDialogListener
) : Dialog(context, R.style.RoundedCornersDialog) {
    companion object {
        fun show(context: Context, theme: UiKitTheme?, listener: CreateDialogListener) {
            CreateDialog(context, theme, listener).show()
        }
    }

    init {
        prepare()
    }

    private fun prepare() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)

        val binding = DialogsCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme(binding)
        applyParams()
        setClickListeners(binding)
    }

    private fun setClickListeners(binding: DialogsCreateBinding) {
        binding.llPrivate.setOnClickListener {
            listener.onClickPrivate()
            dismiss()
        }
        binding.llGroup.setOnClickListener {
            listener.onClickGroup()
            dismiss()
        }
        binding.btnCross.setOnClickListener {
            dismiss()
        }
    }

    private fun applyTheme(binding: DialogsCreateBinding) {
        theme?.getMainBackgroundColor()?.let {
            binding.root.setBackgroundColor(it)
        }
        theme?.getMainTextColor()?.let {
            binding.tvTitle.setTextColor(it)
            binding.btnCross.setColorFilter(it)
        }
        theme?.getMainElementsColor()?.let {
            binding.tvPrivate.setTextColor(it)
            binding.tvGroup.setTextColor(it)
            binding.ivPrivate.setColorFilter(it)
            binding.ivGroup.setColorFilter(it)
            binding.llPrivate.makeClickableBackground(it)
            binding.llGroup.makeClickableBackground(it)
            binding.btnCross.makeClickableBackground(it)
        }
        theme?.getSecondaryBackgroundColor()?.let {
            binding.vDivider.setBackgroundColor(it)
        }
    }

    private fun applyParams() {
        window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
        val params = window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = params
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    interface CreateDialogListener {
        fun onClickPrivate()
        fun onClickGroup()
    }
}