/*
 * Created by Injoit on 27.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.users

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseUsersAdapter
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponent

interface UsersComponent : Component {
    fun getSearchComponent(): SearchComponent?
    fun setVisibleSearch(visible: Boolean)

    fun setAdapter(adapter: BaseUsersAdapter<*>?)
    fun getAdapter(): BaseUsersAdapter<*>?

    fun setItemClickListener(listener: ((result: UserEntity) -> Unit)?)
    fun getItemClickListener(): ((result: UserEntity) -> Unit)?
    fun setOnScrollListener(listener: RecyclerView.OnScrollListener)

    fun setBackground(@ColorInt color: Int)

    fun showSearch(show: Boolean)
    fun showProgress()
    fun hideProgress()
}