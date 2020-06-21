package com.dm6801.frameworkexample

import androidx.lifecycle.lifecycleScope
import com.dm6801.framework.infrastructure.AbstractActivity
import com.dm6801.framework.ui.ProgressBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AbstractActivity() {

    override val layout = R.layout.activity_main
    override val fragmentContainer = R.id.fragment_container
    override val landingClass = FragmentA::class.java

    override fun createProgressBar(isContent: Boolean, isBlocking: Boolean): ProgressBar {
        return CustomProgressBar(isBlocking)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(3_000)
            showProgressBar()
            delay(3_000)
            hideProgressBar()
        }
    }

}