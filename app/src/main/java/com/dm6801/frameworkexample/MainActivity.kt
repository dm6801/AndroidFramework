package com.dm6801.frameworkexample

import android.os.Bundle
import android.widget.Button
import com.dm6801.framework.infrastructure.AbstractActivity
import com.dm6801.framework.ui.onClick
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AbstractActivity() {

    override val layout = R.layout.activity_main
    private val testDialogButton: Button? get() = main_test_dialog_btn
    private val sizedDialogButton: Button? get() = main_size_dialog_btn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initButtons()
    }

    private fun initButtons() {
        testDialogButton?.onClick { TestDialog.open() }
        sizedDialogButton?.onClick { SizedDialog.open() }
    }

}