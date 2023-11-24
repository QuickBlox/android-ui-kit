/*
 * Created by Injoit on 8.11.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogsSelectionAdapter : RecyclerView.Adapter<DialogSelectionViewHolder>(), DialogsAdapter {
    enum class SelectionStrategy { MULTIPLE, SINGLE }

    private var theme: UiKitTheme = LightUIKitTheme()
    private var items: ArrayList<DialogEntity>? = null
    private var strategy: SelectionStrategy = SelectionStrategy.MULTIPLE
    private var listener: DialogAdapterListener? = null
    private var singleSelectionCompleteListener: SingleSelectionCompleteListener? = null
    private var selectedDialogs = mutableListOf<DialogEntity>()
    private var lastCheckedHolder: DialogSelectionViewHolder? = null
    private var forwardedFromDialogId: String? = null

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: DialogSelectionViewHolder, position: Int) {
        val dialogEntity = items?.get(position)
        dialogEntity?.let { dialog ->
            holder.setTheme(theme)
            holder.bind(dialog)

            holder.itemView.setOnClickListener {
                listener?.onClick(dialog)
            }

            holder.itemView.setOnLongClickListener {
                listener?.onLongClick(dialog)
                false
            }

            holder.setCheckBoxListener(object : DialogSelectionViewHolder.CheckBoxListener {
                override fun onSelected(dialog: DialogEntity) {
                    if (strategy == SelectionStrategy.SINGLE) {
                        if (lastCheckedHolder != holder) {
                            lastCheckedHolder?.setChecked(false)
                        }
                        lastCheckedHolder = holder
                    }
                    selectedDialogs.add(dialog)

                    notifySingleSelectionCompleteListener()
                }

                override fun onUnselected(dialog: DialogEntity) {
                    selectedDialogs.remove(dialog)

                    notifySingleSelectionCompleteListener()
                }
            })
        }
    }

    fun getSelectedDialogs(): List<DialogEntity> {
        return selectedDialogs
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    override fun setList(items: ArrayList<DialogEntity>) {
        this.items = items
    }

    fun setSelectionStrategy(strategy: SelectionStrategy) {
        this.strategy = strategy
    }

    fun setSingleSelectionCompleteListener(singleSelectionCompleteListener: SingleSelectionCompleteListener) {
        if (strategy == SelectionStrategy.SINGLE) {
            this.singleSelectionCompleteListener = singleSelectionCompleteListener
        } else {
            throw RuntimeException("You need to set strategy SINGLE for use singleSelectionCompleteListener")
        }
    }

    override fun setDialogAdapterListener(listener: DialogAdapterListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogSelectionViewHolder {
        return DialogSelectionViewHolder.newInstance(parent)
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getDialogId().hashCode().toLong()
    }

    override fun getItemCount(): Int {
        // TODO: Logic for deleting the dialog from which the forwarding occurs
        //   Will be remove in future releases
        val foundDialog = items?.find {
            it.getDialogId() == forwardedFromDialogId
        }
        if (foundDialog != null) {
            items?.remove(foundDialog)
        }
        return items?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        val dialog = items?.get(position)
        return dialog?.getType()?.code ?: DialogEntity.Types.GROUP.code
    }

    private fun notifySingleSelectionCompleteListener() {
        if (strategy == SelectionStrategy.SINGLE && selectedDialogs?.isNotEmpty() == true) {
            singleSelectionCompleteListener?.onSingleSelectionCompleted(true)
        } else {
            singleSelectionCompleteListener?.onSingleSelectionCompleted(false)
        }
    }

    fun setForwardedFromDialogId(dialogId: String?) {
        forwardedFromDialogId = dialogId
    }

    interface SingleSelectionCompleteListener {
        fun onSingleSelectionCompleted(isCompleted: Boolean)
    }
}