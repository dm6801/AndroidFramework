package com.dm6801.framework.ui

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.dm6801.framework.infrastructure.AbstractApplication

object KeyboardManager {

    private val imm: InputMethodManager? get() = AbstractApplication.instance.getSystemService()
    private val activity: AppCompatActivity? get() = AbstractApplication.activity

    fun show(view: View? = null) {
        (view ?: activity?.currentFocus)?.let {
            imm?.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hide(view: View? = null) {
        (view ?: activity?.currentFocus)?.windowToken?.let { window ->
            imm?.hideSoftInputFromWindow(window, 0)
        }
    }

}

fun showKeyboard(view: View? = null) = KeyboardManager.show(view)
fun hideKeyboard(view: View? = null) = KeyboardManager.hide(view)