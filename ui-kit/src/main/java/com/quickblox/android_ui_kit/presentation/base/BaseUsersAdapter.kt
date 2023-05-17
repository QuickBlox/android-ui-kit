/*
 * Created by Injoit on 18.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.base

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class BaseUsersAdapter<T : ViewHolder?> : RecyclerView.Adapter<T>()