/*
 * Created by Injoit on 22.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.databinding.DialogsComponentBinding
import com.quickblox.android_ui_kit.domain.entity.DialogEntity
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponent
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme

class DialogsComponentImpl : LinearLayoutCompat, DialogsComponent {
    private var binding: DialogsComponentBinding? = null
    private var itemClickListener: ((result: DialogEntity) -> Unit)? = null
    private var itemLongClickListener: ((result: DialogEntity) -> Unit)? = null

    private var theme: UiKitTheme = LightUIKitTheme()
    private var adapter: DialogsAdapter? = DialogsAdapter()

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
        val rootView: View = inflate(context, R.layout.dialogs_component, this)
        binding = DialogsComponentBinding.bind(rootView)
        setDefaultState()
    }

    private fun setDefaultState() {
        binding?.rvDialogs?.setHasFixedSize(true)
        binding?.rvDialogs?.background = ColorDrawable(theme.getMainBackgroundColor())
        adapter?.setTheme(theme)
        adapter?.setDialogAdapterListener(DialogAdapterListenerImpl())
        binding?.rvDialogs?.adapter = adapter
        binding?.progressBar?.indeterminateTintList = ColorStateList.valueOf(theme.getMainElementsColor())
        binding?.tvSyncing?.setTextColor(theme.getMainTextColor())
        binding?.llSyncing?.background = ColorDrawable(theme.getMainBackgroundColor())
        binding?.searchComponent?.setTheme(theme)
        binding?.searchComponent?.setSearchButtonNotClickableState()
        binding?.searchComponent?.setMinCharactersLengthForSearch(1)
    }

    override fun getSearchComponent(): SearchComponent? {
        return binding?.searchComponent
    }

    override fun setAdapter(adapter: DialogsAdapter?) {
        this.adapter = adapter
    }

    override fun getAdapter(): DialogsAdapter? {
        return adapter
    }

    override fun setItemClickListener(listener: ((result: DialogEntity) -> Unit)?) {
        itemClickListener = listener
    }

    override fun getItemClickListener(): ((result: DialogEntity) -> Unit)? {
        return itemClickListener
    }

    override fun setItemLongClickListener(listener: ((result: DialogEntity) -> Unit)?) {
        itemLongClickListener = listener
    }

    override fun getItemLongClickListener(): ((result: DialogEntity) -> Unit)? {
        return itemLongClickListener
    }

    override fun showSearch(show: Boolean) {
        if (show) {
            binding?.searchComponent?.visibility = View.VISIBLE
            binding?.searchComponent?.setTheme(theme)
        } else {
            binding?.searchComponent?.visibility = View.GONE
        }
    }

    override fun setTheme(theme: UiKitTheme) {
        this.theme = theme
        setDefaultState()
    }

    override fun getView(): View {
        return this
    }

    override fun showProgressSync() {
        binding?.llSyncing?.visibility = View.VISIBLE
    }

    override fun hideProgressSync() {
        binding?.llSyncing?.visibility = View.GONE
    }

    private inner class DialogAdapterListenerImpl : DialogsAdapter.DialogAdapterListener {
        override fun onClick(dialog: DialogEntity) {
            itemClickListener?.invoke(dialog)
        }

        override fun onLongClick(dialog: DialogEntity) {
            itemLongClickListener?.invoke(dialog)
        }
    }
}