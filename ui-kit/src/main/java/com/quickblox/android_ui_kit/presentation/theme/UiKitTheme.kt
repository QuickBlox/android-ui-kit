package com.quickblox.android_ui_kit.presentation.theme

interface UiKitTheme {
    @Throws(ParseColorException::class)
    fun getMainBackgroundColor(): Int
    fun setMainBackgroundColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getStatusBarColor(): Int
    fun setStatusBarColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getMainElementsColor(): Int
    fun setMainElementsColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getSecondaryBackgroundColor(): Int
    fun setSecondaryBackgroundColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getDisabledElementsColor(): Int
    fun setDisabledElementsColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getMainTextColor(): Int
    fun setMainTextColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getSecondaryTextColor(): Int
    fun setSecondaryTextColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getSecondaryElementsColor(): Int
    fun setSecondaryElementsColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getIncomingMessageColor(): Int
    fun setIncomingMessageColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getOutgoingMessageColor(): Int
    fun setOutgoingMessageColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getDividerColor(): Int
    fun setDividerColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getInputBackgroundColor(): Int
    fun setInputBackgroundColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getTertiaryElementsColor(): Int
    fun setTertiaryElementsColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getCaptionColor(): Int
    fun setCaptionColor(colorString: String)

    @Throws(ParseColorException::class)
    fun getErrorColor(): Int
    fun setErrorColor(colorString: String)

    @Throws(ParseColorException::class)
    fun parseColorToIntFrom(colorString: String): Int
}