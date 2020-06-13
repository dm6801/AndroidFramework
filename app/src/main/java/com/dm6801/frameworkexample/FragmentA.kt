package com.dm6801.frameworkexample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dm6801.framework.infrastructure.AbstractFragment
import com.dm6801.framework.ui.onClick
import kotlinx.android.synthetic.main.fragment_a.*

class FragmentA : AbstractFragment() {

    override val layout = R.layout.fragment_a
    private val testDialogButton: Button? get() = fragment_a_test_dialog_btn
    private val sizedDialogButton: Button? get() = fragment_a_size_dialog_btn
    private val openFragmentB: Button? get() = fragment_a_open_fragment_b
    private val textView: TextView? get() = fragment_a_text_view

    private var args: Map<String, Any?>? = null

    override fun onArguments(arguments: Map<String, Any?>) {
        super.onArguments(arguments)
        args = arguments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
    }

    override fun onForeground() {
        super.onForeground()
        displayArgs()
    }

    private fun displayArgs() {
        args?.map { (key, value) -> "$key: $value" }
            ?.joinToString("\n")
            ?.let { textView?.text = it }
    }

    private fun initButtons() {
        testDialogButton?.onClick { TestDialog.open() }
        sizedDialogButton?.onClick { SizedDialog.open() }
        openFragmentB?.onClick { FragmentB.open() }
    }

}