/*
 * Created by Injoit on 26.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R

fun View.makeClickableBackground(@ColorInt pressedColor: Int? = null) {
    val res = StateListDrawable()
    res.setExitFadeDuration(400)
    res.alpha = 45
    val color: Int = pressedColor ?: ContextCompat.getColor(context, R.color.primary)
    res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(color))
    res.addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
    this.background = res
}

fun View.setVisibility(visible: Boolean) {
    if (visible) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.hideKeyboard() {
    this.post {
        this.context
        this.clearFocus()
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun View.showKeyboard() {
    this.post {
        this.requestFocus()
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}