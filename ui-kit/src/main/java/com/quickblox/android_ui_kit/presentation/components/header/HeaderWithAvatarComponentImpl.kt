/*
 * Created by Injoit on 12.4.2023.
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
import com.quickblox.android_ui_kit.databinding.HeaderWithAvatarComponentBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class HeaderWithAvatarComponentImpl : ConstraintLayout, HeaderWithAvatarComponent {
    private var binding: HeaderWithAvatarComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()
    private var isVisibleAvatar = true

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
        val rootView: View = inflate(context, R.layout.header_with_avatar_component, this)
        binding = HeaderWithAvatarComponentBinding.bind(rootView)

        setListeners()

        setDefaultState()
    }

    private fun setListeners() {
        binding?.btnLeft?.setOnClickListener {
            leftButtonListener?.invoke()
        }

        binding?.btnRight?.setOnClickListener {
            rightButtonListener?.invoke()
        }
    }

    private fun setDefaultState() {
        applyTheme()
    }

    private fun applyTheme() {
        setBackground(theme.getMainBackgroundColor())
        setLeftButtonColor(theme.getMainElementsColor())
        setRightButtonColor(theme.getMainElementsColor())
        setTitleColor(theme.getMainTextColor())
        setDividerColor(theme.getDividerColor())
        binding?.btnLeft?.makeClickableBackground(theme.getMainElementsColor())
        binding?.btnRight?.makeClickableBackground(theme.getMainElementsColor())
    }

    override fun setTitle(title: String?) {
        binding?.tvTitle?.text = title
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

    override fun setRightButtonColor(@ColorInt color: Int) {
        binding?.btnRight?.setColorFilter(color)
    }

    override fun setImageLeftButton(@DrawableRes resource: Int) {
        binding?.btnLeft?.setImageResource(resource)
    }

    override fun setImageRightButton(@DrawableRes resource: Int) {
        binding?.btnRight?.setImageResource(resource)
    }

    override fun loadAvatar(avatarUrl: String, @DrawableRes holder: Int) {
        if (isVisibleAvatar) {
            binding?.ivAvatar?.loadCircleImageFromUrl(avatarUrl, holder)
        }
    }

    override fun setVisibleAvatar(visible: Boolean) {
        binding?.ivAvatar?.setVisibility(visible)
        isVisibleAvatar = visible
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

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }
}