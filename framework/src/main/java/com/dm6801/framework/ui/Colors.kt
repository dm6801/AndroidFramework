package com.dm6801.framework.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.dm6801.framework.R
import com.dm6801.framework.infrastructure.foregroundActivity
import com.dm6801.framework.infrastructure.foregroundApplication

object Colors {

    private val context: Context get() = foregroundActivity ?: foregroundApplication.applicationContext

    var primary: Int = getColor(R.color.colorPrimary)
        set(value) {
            field = value
            primaryStateList = ColorStateList.valueOf(value)
        }
    var primaryStateList: ColorStateList? = null; private set
    var primaryDark: Int = getColor(R.color.colorPrimary)
        set(value) {
            field = value
            primaryDarkStateList = ColorStateList.valueOf(value)
        }
    var primaryDarkStateList: ColorStateList? = null; private set
    var accent: Int = getColor(R.color.colorAccent)
        set(value) {
            field = value
            accentStateList = ColorStateList.valueOf(value)
        }
    var accentStateList: ColorStateList? = null; private set

    fun set(primary: Int? = null, primaryDark: Int? = null, accent: Int? = null) {
        primary?.let { Colors.primary = it }
        primaryDark?.let { Colors.primaryDark = it }
        accent?.let { Colors.accent = it }
    }

    fun setResource(primary: Int? = null, primaryDark: Int? = null, accent: Int? = null) {
        primary?.let { this.primary = getColor(primary) }
        primaryDark?.let { this.primaryDark = getColor(primaryDark) }
        accent?.let { this.accent = getColorOrNull(accent) ?: Color.GRAY }
    }

    fun getColorOrNull(@ColorRes color: Int): Int? {
        return try {
            ContextCompat.getColor(context, color)
        } catch (_: Exception) {
            null
        }
    }

    fun getColor(@ColorRes color: Int): Int {
        return getColorOrNull(color) ?: Color.BLACK
    }

}