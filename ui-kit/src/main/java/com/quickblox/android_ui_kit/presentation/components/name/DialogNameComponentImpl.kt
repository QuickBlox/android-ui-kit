/*
 * Created by Injoit on 25.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.name

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogNameComponentBinding
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class DialogNameComponentImpl : ConstraintLayout, DialogNameComponent {
    private var binding: DialogNameComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()

    private var avatarClickListener: (() -> Unit)? = null

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
        val rootView: View = inflate(context, R.layout.dialog_name_component, this)
        binding = DialogNameComponentBinding.bind(rootView)

        binding?.ivAvatar?.setOnClickListener {
            avatarClickListener?.invoke()
        }

        setDefaultState()
    }

    private fun setDefaultState() {
        setNameHint(context.getString(R.string.enter_name))
        applyTheme()
    }

    private fun applyTheme() {
        setNameTextColor(theme.getMainTextColor())
        setNameHintColor(theme.getDisabledElementsColor())
        setBackground(theme.getMainBackgroundColor())
        binding?.progressBar?.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
    }

    override fun getNameHint(): String {
        return binding?.etName?.hint.toString()
    }

    override fun setNameHint(text: String?) {
        binding?.etName?.hint = text
    }

    @SuppressLint("RestrictedApi")
    override fun setNameHintColor(color: Int) {
        binding?.etName?.setHintTextColor(color)
        binding?.etName?.supportBackgroundTintList = ColorStateList.valueOf(color)
    }

    override fun getNameText(): String {
        return binding?.etName?.text.toString()
    }

    override fun setNameText(text: String?) {
        binding?.etName?.setText(text)
    }

    override fun setNameTextColor(color: Int) {
        binding?.etName?.setTextColor(color)
    }

    override fun setTextWatcherToEditText(textWatcher: TextWatcher) {
        binding?.etName?.addTextChangedListener(textWatcher)
    }

    override fun getAvatarClickListener(): (() -> Unit)? {
        return avatarClickListener
    }

    override fun getAvatarView(): AppCompatImageView? {
        return binding?.ivAvatar
    }

    override fun setAvatarClickListener(listener: (() -> Unit)?) {
        avatarClickListener = listener
    }

    override fun setVisibleAvatar(visible: Boolean) {
        binding?.ivAvatar?.setVisibility(visible)
    }

    override fun setBackground(color: Int) {
        binding?.root?.setBackgroundColor(color)
    }

    override fun setDefaultPlaceHolderAvatar() {
        binding?.ivAvatar?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_preview))
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun showAvatarProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.ivAvatar?.isClickable = false
    }

    override fun hideAvatarProgress() {
        binding?.progressBar?.visibility = View.GONE
        binding?.ivAvatar?.isClickable = true
    }

    override fun getView(): View {
        return this
    }
}