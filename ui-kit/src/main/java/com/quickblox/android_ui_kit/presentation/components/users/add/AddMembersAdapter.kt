/*
 * Created by Injoit on 19.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.users.add

import android.view.ViewGroup
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseUsersAdapter
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class AddMembersAdapter : BaseUsersAdapter<AddMembersViewHolder>() {
    private var items: List<UserEntity>? = null
    private var theme: UiKitTheme = LightUIKitTheme()
    private var addClickListener: ((result: UserEntity?) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<UserEntity>) {
        this.items = items
    }

    fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun setAddUserClickListener(listener: ((result: UserEntity?) -> Unit)) {
        addClickListener = listener
    }

    fun getAddUserClickListener(): ((result: UserEntity?) -> Unit)? {
        return addClickListener
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getUserId()?.toLong() ?: 0L
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddMembersViewHolder {
        return AddMembersViewHolder.newInstance(parent)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: AddMembersViewHolder, position: Int) {
        val userEntity = items?.get(position)

        holder.setTheme(theme)

        holder.bind(userEntity) { user ->
            addClickListener?.invoke(user)
        }
    }
}