/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.users.members

import android.view.ViewGroup
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseUsersAdapter
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class MembersAdapter : BaseUsersAdapter<MemberViewHolder>() {
    private var items: List<UserEntity>? = null
    private var theme: UiKitTheme = LightUIKitTheme()
    private var removeClickListener: ((result: UserEntity?) -> Unit)? = null
    private var loggedUserId: Int? = null
    private var ownerId: Int? = null

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<UserEntity>) {
        this.items = items
    }

    fun setLoggedUserId(loggedUserId: Int?) {
        this.loggedUserId = loggedUserId
    }

    fun setOwnerId(ownerId: Int?) {
        this.ownerId = ownerId
    }

    fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    fun setRemoveClickListener(listener: ((result: UserEntity?) -> Unit)) {
        removeClickListener = listener
    }

    fun getRemoveClickListener(): ((result: UserEntity?) -> Unit)? {
        return removeClickListener
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getUserId()?.toLong() ?: 0L
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder.newInstance(parent)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val userEntity = items?.get(position)

        holder.setTheme(theme)

        val isLoggedUser = userEntity?.getUserId() == loggedUserId
        val isOwner = loggedUserId == ownerId
        holder.bind(userEntity, loggedUserId, ownerId) { user ->
            removeClickListener?.invoke(user)
        }
    }
}