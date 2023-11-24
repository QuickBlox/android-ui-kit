/*
 * Created by Injoit on 5.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.domain.entity.DialogEntity.Types.GROUP
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

class DialogsHistoryAdapter : RecyclerView.Adapter<DialogViewHolder>(), DialogsAdapter {
    private var theme: UiKitTheme = LightUIKitTheme()
    private var items: List<DialogEntity>? = null
    private var listener: DialogAdapterListener? = null

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
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
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
    }

    override fun setList(items: ArrayList<DialogEntity>) {
        this.items = items
    }

    override fun setDialogAdapterListener(listener: DialogAdapterListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        return DialogViewHolder.newInstance(parent)
    }

    override fun getItemId(position: Int): Long {
        return items?.get(position)?.getDialogId().hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        val dialog = items?.get(position)
        return dialog?.getType()?.code ?: GROUP.code
    }
}