package com.dm6801.frameworkexample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.dm6801.framework.infrastructure.foregroundActivity
import com.dm6801.framework.ui.ProgressBar

class CustomProgressBar(private val isBlocking: Boolean) : ProgressBar {

    companion object {
        private const val layout = R.layout.progress_bar_custom
    }

    private val contentView: ViewGroup? get() = foregroundActivity?.contentView

    private val container: ViewGroup? get() = contentView?.findViewById(R.id.custom_progress_bar_container)
    private val progressBarView: ImageView? get() = container?.getChildAt(0) as? ImageView

    override fun show() {
        if (container?.parent == contentView) return
        if (progressBarView?.visibility == View.VISIBLE) return
        contentView?.apply {
            val container = LayoutInflater.from(context).inflate(layout, contentView, true)
            container.apply {
                isClickable = !isBlocking
                isLongClickable = !isBlocking
                isFocusable = !isBlocking
                isFocusableInTouchMode = !isBlocking
            }
        }
    }

    override fun hide() {
        contentView?.removeView(container)
    }

}