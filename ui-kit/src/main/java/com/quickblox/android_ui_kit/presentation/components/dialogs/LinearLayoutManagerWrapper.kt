/*
 * Created by Injoit on 9.1.2025.
 * Copyright © 2025 Quickblox. All rights reserved.
 */

package com.quickblox.android_ui_kit.presentation.components.dialogs

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.android_ui_kit.BuildConfig

class LinearLayoutManagerWrapper(
    context: Context, orientation: Int = RecyclerView.VERTICAL, reverseLayout: Boolean = false,
) : LinearLayoutManager(context, orientation, reverseLayout) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) {
                Log.d("LinearLayoutManager", "IndexOutOfBoundsException in onLayoutChildren", e)
            }
        }
    }
}