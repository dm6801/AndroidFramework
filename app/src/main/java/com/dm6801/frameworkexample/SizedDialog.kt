package com.dm6801.frameworkexample

import android.view.Gravity
import com.dm6801.framework.infrastructure.AbstractDialog

class SizedDialog : AbstractDialog() {

    companion object : Comp<SizedDialog>()

    override val layout = R.layout.dialog_sized
    override val widthFactor = 0.8f
    override val heightFactor = 0.7f
    override val gravity = Gravity.BOTTOM

}