/*
 * Created by Injoit on 13.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.info

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.InfoComponentBinding
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class DialogInfoComponentImpl : LinearLayoutCompat, DialogInfoComponent {
    private var binding: InfoComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()

    private var membersItemListener: (() -> Unit)? = null
    private var leaveItemListener: (() -> Unit)? = null

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
        val rootView: View = inflate(context, R.layout.info_component, this)
        binding = InfoComponentBinding.bind(rootView)

        setListeners()

        applyTheme()
    }

    private fun setListeners() {
        binding?.clMembers?.setOnClickListener {
            membersItemListener?.invoke()
        }

        binding?.clLeave?.setOnClickListener {
            leaveItemListener?.invoke()
        }
    }

    private fun applyTheme() {
        setBackground(theme.getMainBackgroundColor())
        setDividerColor(theme.getDividerColor())
        setTextColorName(theme.getMainTextColor())

        binding?.ivMembers?.setColorFilter(theme.getMainElementsColor())
        binding?.ivLeave?.setColorFilter(theme.getMainElementsColor())
        binding?.ivArrow?.setColorFilter(theme.getSecondaryElementsColor())

        binding?.tvMembers?.setTextColor(theme.getMainTextColor())
        binding?.tvLeave?.setTextColor(theme.getMainTextColor())

        binding?.tvCountMembers?.setTextColor(theme.getSecondaryTextColor())

        binding?.clLeave?.makeClickableBackground(theme.getMainElementsColor())
        binding?.clMembers?.makeClickableBackground(theme.getMainElementsColor())

        binding?.progressBar?.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
    }

    override fun getAvatarView(): AppCompatImageView? {
        return binding?.ivAvatar
    }

    override fun loadAvatar(avatarUrl: String, drawable: Drawable?) {
        binding?.ivAvatar?.loadCircleImageFromUrl(avatarUrl, drawable)
    }

    override fun setDefaultPlaceHolderAvatar() {
        binding?.ivAvatar?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.group_holder))
    }

    override fun setTextName(text: String) {
        binding?.tvName?.text = text
    }

    override fun setTextColorName(color: Int) {
        binding?.tvName?.setTextColor(color)
    }

    override fun getMembersItemClickListener(): (() -> Unit)? {
        return membersItemListener
    }

    override fun setMembersItemClickListener(listener: (() -> Unit)?) {
        membersItemListener = listener
    }

    override fun setVisibleMembersItem(visible: Boolean) {
        binding?.clMembers?.setVisibility(visible)
    }

    override fun setCountMembers(members: Int) {
        binding?.tvCountMembers?.text = members.toString()
    }

    override fun getLeaveItemClickListener(): (() -> Unit)? {
        return leaveItemListener
    }

    override fun setLeaveItemClickListener(listener: (() -> Unit)?) {
        leaveItemListener = listener
    }

    override fun setVisibleLeaveItem(visible: Boolean) {
        binding?.clLeave?.setVisibility(visible)
    }

    override fun setDividerColor(color: Int) {
        binding?.vDivider?.setBackgroundColor(color)
    }

    override fun setBackground(color: Int) {
        binding?.root?.setBackgroundColor(color)
    }

    override fun showProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding?.progressBar?.visibility = View.INVISIBLE
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }
}