package com.dm6801.framework.ui

import android.view.View
import android.widget.Checkable

fun <T : View> T.onClick(disableFor: Long? = 700, action: ((T) -> Unit)?) {
    if (action == null) setOnClickListener(null)
    else setOnClickListener(ViewClickListener(disableFor, action))
}

class ViewClickListener<T : View?, R>(
    disableFor: Long? = NO_TIMEOUT,
    private val block: (T) -> R
) : View.OnClickListener {

    companion object {
        const val DEFAULT_TIMEOUT = 500L
        const val NO_TIMEOUT = -1L
    }

    private var lastClickTime = 0L
    private val disableFor: Long = disableFor ?: DEFAULT_TIMEOUT

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View?) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < disableFor) {
            when (v) {
                is Checkable -> v.isChecked = !v.isChecked
            }
            return
        }
        lastClickTime = now
        (v as? T)?.let { view -> block(view) }

    }

}
