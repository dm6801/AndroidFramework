package com.dm6801.frameworkexample

import com.dm6801.framework.infrastructure.AbstractActivity

class MainActivity : AbstractActivity() {

    override val layout = R.layout.activity_main
    override val fragmentContainer = R.id.fragment_container
    override val landingClass = FragmentA::class.java

}