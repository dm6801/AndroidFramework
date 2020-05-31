package com.dm6801.frameworkexample

import com.dm6801.framework.infrastructure.AbstractDialog

class TestDialog : AbstractDialog() {

    companion object : Comp<TestDialog>()

    override val layout = R.layout.dialog_test

}