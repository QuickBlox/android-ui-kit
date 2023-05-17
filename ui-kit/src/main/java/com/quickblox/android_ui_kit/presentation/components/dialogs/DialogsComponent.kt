/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.dialogs

import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponent

interface DialogsComponent : Component {
    fun getSearchComponent(): SearchComponent?

    fun setAdapter(adapter: DialogsAdapter?)
    fun getAdapter(): DialogsAdapter?

    fun setItemClickListener(listener: ((result: DialogEntity) -> Unit)?)
    fun getItemClickListener(): ((result: DialogEntity) -> Unit)?

    fun setItemLongClickListener(listener: ((result: DialogEntity) -> Unit)?)
    fun getItemLongClickListener(): ((result: DialogEntity) -> Unit)?

    fun showSearch(show: Boolean)

    fun showProgressSync()
    fun hideProgressSync()
}