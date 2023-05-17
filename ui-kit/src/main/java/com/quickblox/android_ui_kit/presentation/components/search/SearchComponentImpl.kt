/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.SearchComponentBinding
import com.quickblox.android_ui_kit.presentation.hideKeyboard
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.SimpleTextWatcher
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class SearchComponentImpl : ConstraintLayout, SearchComponent {
    private var binding: SearchComponentBinding? = null
    private var theme: UiKitTheme = LightUIKitTheme()

    private var searchEventListener: SearchEventListener? = null
    private var minCharactersLength = 1

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
        val rootView: View = inflate(context, R.layout.search_component, this)
        binding = SearchComponentBinding.bind(rootView)

        setListeners()
        setDefaultState()
    }

    private fun setListeners() {
        binding?.btnSearch?.setOnClick {
            it.hideKeyboard()
            val text = binding?.etSearch?.text.toString()
            searchEventListener?.onSearchEvent(text)
        }
        binding?.ivClear?.setOnClickListener {
            it.hideKeyboard()
            binding?.etSearch?.text?.clear()
        }
    }

    private fun setDefaultState() {
        setSearchHint(context.getString(R.string.search_component_hint))
        setDefaultTextWatcher()
        applyTheme()
    }

    private fun setDefaultTextWatcher() {
        setTextWatcherToEditText(object : SimpleTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val text = charSequence.toString()
                if (text.isEmpty()) {
                    searchEventListener?.onDefaultEvent()
                    setSearchButtonNotClickableState()
                    return
                }

                val isValidText = text.length >= minCharactersLength
                if (isValidText) {
                    setSearchButtonClickableState()
                } else {
                    setSearchButtonNotClickableState()
                }
            }
        })
    }

    private fun applyTheme() {
        setSearchTextColor(theme.getMainTextColor())
        setSearchHintColor(theme.getSecondaryTextColor())
        setBackground(theme.getMainBackgroundColor())
        setDividerColor(theme.getDividerColor())
        binding?.btnSearch?.makeClickableBackground(theme.getMainElementsColor())
    }

    override fun setSearchHint(text: String?) {
        binding?.etSearch?.hint = text
    }

    override fun getSearchHint(): String {
        return binding?.etSearch?.hint.toString()
    }

    @SuppressLint("RestrictedApi")
    override fun setSearchHintColor(@ColorInt color: Int) {
        binding?.etSearch?.setHintTextColor(color)
        binding?.etSearch?.supportBackgroundTintList = ColorStateList.valueOf(color)
    }

    override fun getSearchText(): String {
        return binding?.etSearch?.text.toString()
    }

    override fun setSearchText(text: String?) {
        binding?.etSearch?.setText(text)
    }

    override fun setTextWatcherToEditText(textWatcher: TextWatcher) {
        binding?.etSearch?.addTextChangedListener(textWatcher)
    }

    override fun setSearchTextColor(@ColorInt color: Int) {
        binding?.etSearch?.setTextColor(color)
    }

    override fun setMinCharactersLengthForSearch(length: Int) {
        minCharactersLength = length
    }

    override fun setVisibleSearchButton(visible: Boolean) {
        if (visible) {
            binding?.btnSearch?.visibility = View.VISIBLE
        } else {
            binding?.btnSearch?.visibility = View.GONE
        }
    }

    override fun setSearchButtonClickableState() {
        setSearchButtonColor(theme.getMainElementsColor())
        binding?.btnSearch?.isClickable = true
    }

    override fun setSearchButtonNotClickableState() {
        setSearchButtonColor(theme.getDisabledElementsColor())
        binding?.btnSearch?.isClickable = false
    }

    override fun setSearchClickListener(searchEventListener: SearchEventListener?) {
        this.searchEventListener = searchEventListener
    }

    override fun getSearchClickListener(): SearchEventListener? {
        return searchEventListener
    }

    override fun setSearchButtonColor(@ColorInt color: Int) {
        binding?.btnSearch?.setColorFilter(color)
    }

    override fun setImageSearchButton(@DrawableRes resource: Int) {
        binding?.btnSearch?.setImageResource(resource)
    }

    override fun setDividerColor(@ColorInt color: Int) {
        binding?.vDivider?.setBackgroundColor(color)
    }

    override fun setBackground(color: Int) {
        binding?.root?.setBackgroundColor(color)
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }

    interface SearchEventListener {
        fun onSearchEvent(text: String)
        fun onDefaultEvent()
    }
}