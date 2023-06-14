/*
 * Created by Injoit on 21.2.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.android_ui_kit.presentation.components.search

import android.text.TextWatcher
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.quickblox.android_ui_kit.presentation.components.Component
import com.quickblox.android_ui_kit.presentation.components.search.SearchComponentImpl.SearchEventListener

interface SearchComponent : Component {
    fun getSearchHint(): String
    fun setSearchHint(text: String?)
    fun setSearchHintColor(@ColorInt color: Int)

    fun getSearchText(): String
    fun setSearchText(text: String?)
    fun setTextWatcherToEditText(textWatcher: TextWatcher?)
    fun setSearchTextColor(@ColorInt color: Int)
    fun setMinCharactersLengthForSearch(number: Int)
    fun clearSearchTextAndReinitTextWatcher()

    fun getSearchClickListener(): SearchEventListener?
    fun setSearchClickListener(searchEventListener: SearchEventListener?)
    fun setVisibleSearchButton(visible: Boolean)
    fun setSearchButtonClickableState()
    fun setSearchButtonNotClickableState()

    fun setSearchButtonColor(@ColorInt color: Int)
    fun setImageSearchButton(@DrawableRes resource: Int)

    fun setDividerColor(@ColorInt color: Int)
    fun setBackground(@ColorInt color: Int)
}