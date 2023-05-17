/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.dialogs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.MenuItemBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground

class MenuItem : FrameLayout {
    private var binding: MenuItemBinding? = null
    private var itemClickListener: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val rootView: View = inflate(context, R.layout.menu_item, this)
        binding = MenuItemBinding.bind(rootView)
        binding?.tvItem?.setOnClickListener {
            itemClickListener?.invoke()
        }
    }

    fun setText(text: String) {
        binding?.tvItem?.text = text
    }

    fun setColorText(@ColorInt color: Int) {
        binding?.tvItem?.setTextColor(color)
    }

    fun setRipple(@ColorInt rippleColor: Int) {
        binding?.tvItem?.makeClickableBackground(rippleColor)
    }

    fun setItemClickListener(listener: (() -> Unit)?) {
        itemClickListener = listener
    }

    fun getItemClickListener(): (() -> Unit)? {
        return itemClickListener
    }
}