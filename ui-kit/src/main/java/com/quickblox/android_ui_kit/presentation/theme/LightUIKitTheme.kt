package com.quickblox.android_ui_kit.presentation.theme

import android.graphics.Color

class LightUIKitTheme : UiKitTheme {
    private var mainBackgroundColor: String = "#FFFFFF"
    private var statusBarColor: String = "#E4E6E8"
    private var mainElementsColor: String = "#3978FC"
    private var secondaryBackgroundColor: String = "#E7EFFF"
    private var mainTextColor: String = "#0B121B"
    private var disabledElementsColor: String = "#BCC1C5"
    private var secondaryTextColor: String = "#636D78"
    private var secondaryElementsColor: String = "#202F3E"
    private var dividerColor: String = "#E7EFFF"
    private var incomingMessageColor: String = "#E4E6E8"
    private var outgoingMessageColor: String = "#E7EFFF"
    private var inputBackgroundColor: String = "#E4E6E8"
    private var tertiaryElementsColor: String = "#636D78"
    private var captionColor: String = "#90979F"
    private var errorColor: String = "#FF766E"

    override fun getMainBackgroundColor(): Int {
        return parseColorToIntFrom(mainBackgroundColor)
    }

    override fun setMainBackgroundColor(colorString: String) {
        mainBackgroundColor = colorString
    }

    override fun getStatusBarColor(): Int {
        return parseColorToIntFrom(statusBarColor)
    }

    override fun setStatusBarColor(colorString: String) {
        statusBarColor = colorString
    }

    override fun getMainElementsColor(): Int {
        return parseColorToIntFrom(mainElementsColor)
    }

    override fun setMainElementsColor(colorString: String) {
        mainElementsColor = colorString
    }

    override fun getSecondaryBackgroundColor(): Int {
        return parseColorToIntFrom(secondaryBackgroundColor)
    }

    override fun setSecondaryBackgroundColor(colorString: String) {
        secondaryBackgroundColor = colorString
    }

    override fun setDisabledElementsColor(colorString: String) {
        disabledElementsColor = colorString
    }

    override fun getDisabledElementsColor(): Int {
        return parseColorToIntFrom(disabledElementsColor)
    }

    override fun getMainTextColor(): Int {
        return parseColorToIntFrom(mainTextColor)
    }

    override fun setMainTextColor(colorString: String) {
        mainTextColor = colorString
    }

    override fun setSecondaryTextColor(colorString: String) {
        secondaryTextColor = colorString
    }

    override fun getSecondaryTextColor(): Int {
        return parseColorToIntFrom(secondaryTextColor)
    }

    override fun setSecondaryElementsColor(colorString: String) {
        secondaryElementsColor = colorString
    }

    override fun getIncomingMessageColor(): Int {
        return parseColorToIntFrom(incomingMessageColor)
    }

    override fun setIncomingMessageColor(colorString: String) {
        incomingMessageColor = colorString
    }

    override fun getOutgoingMessageColor(): Int {
        return parseColorToIntFrom(outgoingMessageColor)
    }

    override fun setOutgoingMessageColor(colorString: String) {
        outgoingMessageColor = colorString
    }

    override fun getDividerColor(): Int {
        return parseColorToIntFrom(dividerColor)
    }

    override fun setDividerColor(colorString: String) {
        dividerColor = colorString
    }

    override fun getInputBackgroundColor(): Int {
        return parseColorToIntFrom(inputBackgroundColor)
    }

    override fun setInputBackgroundColor(colorString: String) {
        inputBackgroundColor = colorString
    }

    override fun getTertiaryElementsColor(): Int {
        return parseColorToIntFrom(tertiaryElementsColor)
    }

    override fun setTertiaryElementsColor(colorString: String) {
        tertiaryElementsColor = colorString
    }

    override fun getCaptionColor(): Int {
        return parseColorToIntFrom(captionColor)
    }

    override fun setCaptionColor(colorString: String) {
       captionColor = colorString
    }

    override fun getSecondaryElementsColor(): Int {
        return parseColorToIntFrom(secondaryElementsColor)
    }

    override fun getErrorColor(): Int {
        return parseColorToIntFrom(errorColor)
    }

    override fun setErrorColor(colorString: String) {
        errorColor = colorString
    }

    override fun parseColorToIntFrom(colorString: String): Int {
        try {
            return Color.parseColor(colorString)
        } catch (exception: IllegalArgumentException) {
            throw ParseColorException(exception.message.toString())
        } catch (exception: NumberFormatException) {
            throw ParseColorException(exception.message.toString())
        }
    }
}