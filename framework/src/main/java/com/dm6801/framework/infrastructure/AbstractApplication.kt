package com.dm6801.framework.infrastructure

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dm6801.framework.utilities.Log

val foregroundApplication: AbstractApplication get() = AbstractApplication.instance
val foregroundActivity: AbstractActivity? get() = AbstractApplication.activity as? AbstractActivity
val foregroundFragment: AbstractFragment? get() = AbstractApplication.fragment

abstract class AbstractApplication : Application() {

    companion object {
        lateinit var instance: AbstractApplication
        val activity: AppCompatActivity? get() = ActivityLifecycleObserver.foregroundActivity
        val fragment: AbstractFragment?
            get() = activity?.supportFragmentManager?.fragments
                ?.filterIsInstance<AbstractFragment>()
                ?.lastOrNull()
    }

    override fun onCreate() {
        instance = this
        Log("onCreate(): $instance")
        super.onCreate()
        ActivityLifecycleObserver.init()
    }

}