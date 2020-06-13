package com.dm6801.frameworkexample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.dm6801.framework.infrastructure.AbstractFragment
import com.dm6801.framework.ui.onClick
import kotlinx.android.synthetic.main.fragment_b.*

class FragmentB : AbstractFragment() {

    companion object : Comp()

    override val layout = R.layout.fragment_b
    private val argsEdit: EditText? get() = fragment_b_args
    private val argsBack: Button? get() = fragment_b_back_with_args

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }

    private fun initButton() {
        argsBack?.onClick {
            navigateBack("editText" to argsEdit?.text?.toString())
        }
    }

}