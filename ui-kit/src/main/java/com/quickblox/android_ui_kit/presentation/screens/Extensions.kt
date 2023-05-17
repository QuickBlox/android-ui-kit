/*
 * Created by Injoit on 21.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.presentation.screens

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

fun modifyDialogsDateStringFrom(time: Long): String {
    if (time <= 0L) {
        return ""
    }

    val calendarWithMessageTime = getCalendarWith(time)
    val isYesterday = isYesterday(calendarWithMessageTime)

    if (isYesterday) {
        return "yesterday"
    }

    val isCurrentYear = isCurrentYear(calendarWithMessageTime)
    val isToday = isToday(calendarWithMessageTime)
    val format = when {
        isToday -> {
            SimpleDateFormat("HH:mm", Locale.ENGLISH)
        }
        isCurrentYear -> {
            SimpleDateFormat("d MMM", Locale.ENGLISH)
        }
        else -> {
            SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)
        }
    }
    return format.format(Date(calendarWithMessageTime.timeInMillis))
}

fun modifyChatDateStringFrom(time: Long): String {
    if (time <= 0L) {
        return ""
    }

    val calendarWithMessageTime = getCalendarWith(time)
    val isToday = isToday(calendarWithMessageTime)
    if (isToday) {
        return "Today"
    }

    val isYesterday = isYesterday(calendarWithMessageTime)
    if (isYesterday) {
        return "Yesterday"
    }

    val isCurrentYear = isCurrentYear(calendarWithMessageTime)
    val format = when {
        isCurrentYear -> {
            SimpleDateFormat("d MMM", Locale.ENGLISH)
        }
        else -> {
            SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)
        }
    }
    return format.format(Date(calendarWithMessageTime.timeInMillis))
}

fun Long.convertToStringTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(this * 1000))
}

private fun getCalendarWith(time: Long): Calendar {
    val calendar = Calendar.getInstance()
    val timeInMillis = time * 1000
    calendar.timeInMillis = timeInMillis
    return calendar
}

private fun isCurrentYear(calendar: Calendar): Boolean {
    return Calendar.getInstance()[Calendar.YEAR] == calendar[Calendar.YEAR]
}

private fun isYesterday(calendar: Calendar): Boolean {
    val isOneDayBefore = Calendar.getInstance()[Calendar.DAY_OF_YEAR] - calendar[Calendar.DAY_OF_YEAR] == 1
    return isCurrentYear(calendar) && isOneDayBefore
}

private fun isToday(calendar: Calendar): Boolean {
    val currentCalendar = Calendar.getInstance()
    val isCurrentYear = currentCalendar[Calendar.YEAR] == calendar[Calendar.YEAR]
    val isCurrentMonth = currentCalendar[Calendar.MONTH] == calendar[Calendar.MONTH]
    val isCurrentDate = currentCalendar[Calendar.DATE] == calendar[Calendar.DATE]

    return isCurrentYear && isCurrentMonth && isCurrentDate
}

fun ImageView.loadCircleImageFromUrl(imageUrl: String?, @DrawableRes placeholder: Int) {
    Glide.with(this.context)
        .load(imageUrl)
        .centerCrop()
        .circleCrop()
        .placeholder(placeholder)
        .error(placeholder)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
        .into(this)
}

fun ImageView.loadCircleImageFromUrl(imageUrl: String?, drawable: Drawable?) {
    Glide.with(this.context)
        .load(imageUrl)
        .centerCrop()
        .circleCrop()
        .placeholder(drawable)
        .error(drawable)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
        .into(this)
}

fun ImageView.loadCircleImageFromUri(imageUri: Uri?, @DrawableRes placeholder: Int) {
    Glide.with(this.context).load(imageUri).centerCrop().circleCrop().placeholder(placeholder).into(this)
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

private const val MIN_DIALOG_NAME_LENGTH = 3
private const val MAX_DIALOG_NAME_LENGTH = 60

fun String.isValidDialogName(): Boolean {
    return this.length in MIN_DIALOG_NAME_LENGTH..MAX_DIALOG_NAME_LENGTH
}

fun View.setOnClick(doClick: (View) -> Unit) = setOnClickListener(DebouchingOnClickListener(doClick = doClick))

private const val CLICK_INTERVAL = 1500L

class DebouchingOnClickListener(private val doClick: ((View) -> Unit)) : View.OnClickListener {
    companion object {
        @JvmStatic
        var enabled = true
        private val ENABLE_AGAIN = Runnable { enabled = true }
    }

    override fun onClick(view: View) {
        if (enabled) {
            enabled = false
            view.postDelayed(ENABLE_AGAIN, CLICK_INTERVAL)
            doClick(view)
        }
    }
}

open class SimpleTextWatcher : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
    }
}