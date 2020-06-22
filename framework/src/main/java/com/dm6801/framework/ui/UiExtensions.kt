@file:Suppress("unused", "UNCHECKED_CAST", "LocalVariableName")

package com.dm6801.framework.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.children
import androidx.core.view.isVisible
import com.dm6801.framework.infrastructure.AbstractApplication
import com.dm6801.framework.utilities.catch

private val app get() = AbstractApplication.instance
private val activity get() = AbstractApplication.activity
private val context: Context get() = activity ?: app
private val resources: Resources get() = activity?.resources ?: app.resources

fun getColor(@ColorRes res: Int): Int? = catch {
    ContextCompat.getColor(context, res)
}

fun getDrawable(@DrawableRes res: Int): Drawable? =
    catch {
        AppCompatResources.getDrawable(context, res)
    }

fun getString(@StringRes res: Int, vararg args: Any): String? =
    catch {
        resources.getString(res, *args)
    }

val windowManager: WindowManager? get() = app.getSystemService()

fun getScreenSize(): Pair<Int, Int> {
    val windowManager = windowManager ?: return 0 to 0
    val dm = DisplayMetrics()
    windowManager.defaultDisplay.getRealMetrics(dm)
    return dm.widthPixels to dm.heightPixels
}

val Int.dpToPx: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

val Int.pxToDp: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

fun View?.isVisibleOnScreen(): Boolean {
    if (this == null || !this.isShown) return false
    val (screenWidth, screenHeight) = getScreenSize()
    val actualPosition = Rect()
    this.getGlobalVisibleRect(actualPosition)
    val screen = Rect(0, 0, screenWidth, screenHeight)
    return actualPosition.intersect(screen)
}

fun View.clickable(isClickable: Boolean = true) {
    this.isClickable = isClickable
    this.isFocusable = isClickable
    this.isFocusableInTouchMode = isClickable
}

fun View?.wasClicked(motionEvent: MotionEvent): Boolean {
    if (this?.isVisible == false) return false

    val viewCoords = intArrayOf(0, 0)
    this?.getLocationOnScreen(viewCoords) ?: return false

    val x = viewCoords[0]
    val y = viewCoords[1]
    val viewHitRect = Rect(x, y, x + width, y + height)

    return viewHitRect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())
}

inline fun <reified T> ViewGroup.findChild(noinline predicate: ((T) -> Boolean)? = null): T? =
    findChild(T::class.java, predicate)

fun <T> ViewGroup.findChild(clazz: Class<T>, predicate: ((T) -> Boolean)? = null): T? {
    try {
        val _predicate = if (predicate != null) {
            { view: View -> clazz.isAssignableFrom(view.javaClass) && predicate(view as T) }
        } else {
            { view: View -> clazz.isAssignableFrom(view.javaClass) }
        }

        var result: T? = children.firstOrNull(predicate = _predicate) as? T
        if (result == null)
            for (child in children) {
                if (child is ViewGroup) result = child.findChild(clazz, predicate)
                if (result != null) break
            }
        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun View.updateMargins(
    all: Int? = null,
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    try {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(
            left ?: all ?: params.leftMargin,
            top ?: all ?: params.topMargin,
            right ?: all ?: params.rightMargin,
            bottom ?: all ?: params.bottomMargin
        )
        params.marginStart = left ?: all ?: params.marginStart
        params.marginEnd = right ?: all ?: params.marginEnd
        layoutParams = params
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.updatePadding(
    all: Int? = null,
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    try {
        setPadding(
            left ?: all ?: paddingLeft,
            top ?: all ?: paddingTop,
            right ?: all ?: paddingRight,
            bottom ?: all ?: paddingBottom
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

val View.resourceName: String? get() = catch { resources?.getResourceName(id) }

val View.resourceEntryName: String? get() = catch { resources?.getResourceEntryName(id) }

val navigationBarHeight: Int
    get() {
        return try {
            val hasNavBarResId =
                resources.getIdentifier("config_showNavigationBar", "bool", "android")
            val heightResId =
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val hasNavBar = if (hasNavBarResId > 0) resources.getBoolean(hasNavBarResId) else false
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            return if (hasNavBar || !hasMenuKey && !hasBackKey)
                resources.getDimensionPixelSize(heightResId)
            else 0
        } catch (_: Exception) {
            0
        }
    }

val statusBarHeight: Int
    get() {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId)
        else 0
    }

fun TextView.setText(text: Any?) {
    when (text) {
        is Int -> setText(text)
        is String -> setText(text)
        is CharSequence -> setText(text)
        is CharArray -> setText(text, 0, text.size)
        else -> setText(text?.toString())
    }
}

