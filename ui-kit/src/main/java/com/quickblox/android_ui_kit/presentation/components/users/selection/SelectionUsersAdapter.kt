/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.users.selection

import android.view.ViewGroup
import androidx.collection.ArraySet
import com.quickblox.android_ui_kit.domain.entity.UserEntity
import com.quickblox.android_ui_kit.presentation.base.BaseUsersAdapter
import com.quickblox.android_ui_kit.presentation.components.users.selection.SelectionUsersAdapter.SelectionStrategy.SINGLE
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class SelectionUsersAdapter : BaseUsersAdapter<SelectionUserViewHolder>() {
    enum class SelectionStrategy { MULTIPLE, SINGLE }

    private var lastCheckedHolder: SelectionUserViewHolder? = null
    private var items: List<UserEntity>? = null
    private var selectedUsers: ArraySet<UserEntity>? = null
    private var theme: UiKitTheme = LightUIKitTheme()
    private var strategy: SelectionStrategy = SelectionStrategy.MULTIPLE
    private var singleSelectionCompleteListener: SingleSelectionCompleteListener? = null

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<UserEntity>) {
        this.items = items
    }

    fun setSelectedUsers(selectedUsers: ArraySet<UserEntity>) {
        this.selectedUsers = selectedUsers
    }

    fun getSelectedUsers(): Collection<UserEntity>? {
        return selectedUsers
    }

    fun setSelectionStrategy(strategy: SelectionStrategy) {
        this.strategy = strategy
    }

    fun setSingleSelectionCompleteListener(singleSelectionCompleteListener: SingleSelectionCompleteListener) {
        if (strategy == SINGLE) {
            this.singleSelectionCompleteListener = singleSelectionCompleteListener
        } else {
            throw RuntimeException("You need to set strategy SINGLE for use singleSelectionCompleteListener")
        }
    }

    fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getUserId()?.toLong() ?: 0L
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionUserViewHolder {
        return SelectionUserViewHolder.newInstance(parent)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: SelectionUserViewHolder, position: Int) {
        val userEntity = items?.get(position)
        val isSelected = selectedUsers?.contains(userEntity)
        holder.setTheme(theme)
        isSelected?.let {
            holder.bind(userEntity, it)
        }

        holder.setCheckBoxListener(object : SelectionUserViewHolder.CheckBoxListener {
            override fun onSelected(user: UserEntity?) {
                if (strategy == SINGLE) {
                    if (lastCheckedHolder != holder) {
                        lastCheckedHolder?.setChecked(false)
                    }
                    lastCheckedHolder = holder
                }
                selectedUsers?.add(user)

                notifySingleSelectionCompleteListener()
            }

            override fun onUnselected(user: UserEntity?) {
                selectedUsers?.remove(user)

                notifySingleSelectionCompleteListener()
            }
        })
    }

    private fun notifySingleSelectionCompleteListener() {
        if (strategy == SINGLE && selectedUsers?.isNotEmpty() == true) {
            singleSelectionCompleteListener?.onSingleSelectionCompleted(true)
        } else {
            singleSelectionCompleteListener?.onSingleSelectionCompleted(false)
        }
    }

    interface SingleSelectionCompleteListener {
        fun onSingleSelectionCompleted(isCompleted: Boolean)
    }
}