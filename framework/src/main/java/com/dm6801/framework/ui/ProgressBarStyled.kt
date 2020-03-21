package com.dm6801.framework.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.widget.ContentLoadingProgressBar
import com.dm6801.framework.R

open class ProgressBarStyled @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    root: ViewGroup? = null,
    val isContentLoading: Boolean = false,
    val isBlocking: Boolean = true
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        try {
            val layout =
                if (isContentLoading) R.layout.progress_bar_content_loading else R.layout.progress_bar
            val container = inflate(context, layout, this) as? ViewGroup
            (container?.getChildAt(0) as? ProgressBar)?.apply {
                setColors(this)
            }
            root?.addView(container)
            container?.apply {
                layoutParams = layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                isClickable = isBlocking
                isFocusable = isBlocking
                isFocusableInTouchMode = isBlocking
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun setColors(progressBar: ProgressBar) = progressBar.apply {
        indeterminateTintList = Colors.accentStateList
        progressTintList = Colors.accentStateList
        secondaryProgressTintList = Colors.primaryDarkStateList
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            foregroundTintList = Colors.accentStateList
    }

    private val container: ViewGroup?
        get() = getChildAt(0) as? ViewGroup

    private val progressBar: ProgressBar?
        get() = container?.getChildAt(0) as? ProgressBar

    fun show() {
        (progressBar as? ContentLoadingProgressBar)?.show()
    }

    fun hide() {
        (progressBar as? ContentLoadingProgressBar)?.hide()
        (parent as? ViewGroup)?.removeView(this)
    }

}