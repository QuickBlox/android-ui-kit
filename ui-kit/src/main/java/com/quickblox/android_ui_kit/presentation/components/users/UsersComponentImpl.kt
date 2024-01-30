/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.users

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.UsersComponentBinding
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseUsersAdapter
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponent
import com.quickblox.android_ui_kit.presentation.setVisibility
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class UsersComponentImpl : CoordinatorLayout, UsersComponent {
    private var theme: UiKitTheme = LightUIKitTheme()

    private var binding: UsersComponentBinding? = null
    private var itemClickListener: ((result: UserEntity) -> Unit)? = null

    private var adapter: BaseUsersAdapter<*>? = null

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
        val rootView: View = inflate(context, R.layout.users_component, this)
        binding = UsersComponentBinding.bind(rootView)
        applyTheme()
    }

    private fun applyTheme() {
        val mainColor = theme.getMainBackgroundColor()
        binding?.rvUsers?.setBackgroundColor(mainColor)
        binding?.searchComponent?.setTheme(theme)
        binding?.progressBar?.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
        binding?.searchComponent?.setSearchButtonNotClickableState()
        binding?.searchComponent?.setMinCharactersLengthForSearch(3)
    }

    override fun getSearchComponent(): SearchComponent? {
        return binding?.searchComponent
    }

    override fun setVisibleSearch(visible: Boolean) {
        binding?.searchComponent?.setVisibility(visible)
    }

    override fun setAdapter(adapter: BaseUsersAdapter<*>?) {
        this.adapter = adapter
        binding?.rvUsers?.adapter = adapter
    }

    override fun getAdapter(): BaseUsersAdapter<*>? {
        return adapter
    }

    override fun setItemClickListener(listener: ((result: UserEntity) -> Unit)?) {
        itemClickListener = listener
    }

    override fun getItemClickListener(): ((result: UserEntity) -> Unit)? {
        return itemClickListener
    }

    override fun showSearch(show: Boolean) {
        if (show) {
            binding?.searchComponent?.visibility = View.VISIBLE
            binding?.searchComponent?.setTheme(theme)
        } else {
            binding?.searchComponent?.visibility = View.GONE
        }
    }

    override fun showProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding?.progressBar?.visibility = View.GONE
    }

    override fun setBackground(color: Int) {
        binding?.root?.setBackgroundColor(color)
    }

    override fun setOnScrollListener(listener: RecyclerView.OnScrollListener) {
        binding?.rvUsers?.addOnScrollListener(listener)
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        applyTheme()
    }

    override fun getView(): View {
        return this
    }
}