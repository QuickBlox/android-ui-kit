/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.base

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.BaseDialogBinding
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

abstract class BaseDialog(context: Context, private val title: String?, protected val themeDialog: UiKitTheme?) :
    Dialog(context, R.style.RoundedCornersDialog) {
    init {
        prepare()
    }

    private fun prepare() {
        val binding = BaseDialogBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        applyParams()

        val isNotNullAndNotEmptyTitle =  !title.isNullOrEmpty()
        if (isNotNullAndNotEmptyTitle) {
            setTitleAndShow(binding, title)
        }

        themeDialog?.getMainBackgroundColor()?.let {
            binding.root.setBackgroundColor(it)
        }

        val views = collectViewsTemplateMethod()
        for (view in views) {
            view?.let {
                binding.llContainer.addView(view)
            }
        }
    }

    private fun applyParams() {
        setCancelable(true)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setTitleAndShow(binding: BaseDialogBinding, title: String?) {
        binding.tvTitle.text = title
        themeDialog?.getMainTextColor()?.let {
            binding.tvTitle.setTextColor(it)
        }
        binding.tvTitle.visibility = View.VISIBLE

    }

    abstract fun collectViewsTemplateMethod(): List<View?>
}