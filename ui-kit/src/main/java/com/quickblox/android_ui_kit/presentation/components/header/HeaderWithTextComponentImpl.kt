/*
 * Created by Injoit on 23.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.header

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.HeaderWithTextComponentBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class HeaderWithTextComponentImpl : ConstraintLayout, HeaderWithTextComponent {
    private var binding: HeaderWithTextComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()

    private var leftButtonListener: (() -> Unit)? = null
    private var rightButtonListener: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val rootView: View = inflate(context, R.layout.header_with_text_component, this)
        binding = HeaderWithTextComponentBinding.bind(rootView)

        setListeners()

        setDefaultState()
    }

    private fun setListeners() {
        binding?.btnLeft?.setOnClickListener {
            leftButtonListener?.invoke()
        }

        binding?.btnRight?.setOnClick {
            rightButtonListener?.invoke()
        }
    }

    private fun setDefaultState() {
        setTitle(context.getString(R.string.create_dialog))
        applyTheme()
    }

    private fun applyTheme() {
        setBackground(theme.getMainBackgroundColor())
        setLeftButtonColor(theme.getMainElementsColor())
        setRightButtonTextColor(theme.getMainElementsColor())
        setTitleColor(theme.getMainTextColor())
        setDividerColor(theme.getDividerColor())
        binding?.btnLeft?.makeClickableBackground(theme.getMainElementsColor())
        binding?.btnRight?.makeClickableBackground(theme.getMainElementsColor())
    }

    override fun setTitle(title: String?) {
        binding?.tvTitle?.text = title ?: context.getString(R.string.create_dialog)
    }

    override fun getTitle(): String? {
        return binding?.tvTitle?.text.toString()
    }

    override fun setTitleColor(@ColorInt color: Int) {
        binding?.tvTitle?.setTextColor(color)
    }

    override fun setLeftButtonClickListener(listener: (() -> Unit)?) {
        leftButtonListener = listener
    }

    override fun getLeftButtonClickListener(): (() -> Unit)? {
        return leftButtonListener
    }

    override fun setRightButtonClickListener(listener: (() -> Unit)?) {
        rightButtonListener = listener
    }

    override fun getRightButtonClickListener(): (() -> Unit)? {
        return rightButtonListener
    }

    override fun setLeftButtonColor(@ColorInt color: Int) {
        binding?.btnLeft?.setColorFilter(color)
    }

    override fun setRightButtonTextColor(@ColorInt color: Int) {
        binding?.btnRight?.setTextColor(color)
    }

    override fun setImageLeftButton(@DrawableRes resource: Int) {
        binding?.btnLeft?.setImageResource(resource)
    }

    override fun setTextRightButton(text: String) {
        binding?.btnRight?.text = text
    }

    override fun setVisibleLeftButton(visible: Boolean) {
        binding?.btnLeft?.setVisibility(visible)
    }

    override fun setVisibleRightButton(visible: Boolean) {
        binding?.btnRight?.setVisibility(visible)
    }

    override fun setVisibleTitle(visible: Boolean) {
        binding?.tvTitle?.setVisibility(visible)
    }

    override fun setDividerColor(color: Int) {
        binding?.vDivider?.setBackgroundColor(color)
    }

    override fun setBackground(color: Int) {
        binding?.clParent?.setBackgroundColor(color)
    }

    override fun setRightButtonClickableState() {
        setRightButtonTextColor(theme.getMainElementsColor())
        binding?.btnRight?.isClickable = true
    }

    override fun setRightButtonNotClickableState() {
        setRightButtonTextColor(theme.getDisabledElementsColor())
        binding?.btnRight?.isClickable = false
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }
}