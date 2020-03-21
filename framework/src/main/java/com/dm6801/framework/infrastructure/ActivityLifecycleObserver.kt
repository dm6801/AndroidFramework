package com.dm6801.framework.infrastructure

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dm6801.framework.utilities.Log
import java.lang.ref.WeakReference

@Suppress("ObjectPropertyName", "MemberVisibilityCanBePrivate")
object ActivityLifecycleObserver : Application.ActivityLifecycleCallbacks {

    private val app get() = AbstractApplication.instance
    val foregroundActivity: AppCompatActivity? get() = _foregroundActivity?.get()
    var _foregroundActivity: WeakReference<AppCompatActivity?>? = null
    val foregroundActivityLive: LiveData<AppCompatActivity?> = MutableLiveData()
    val activitiesLive: LiveData<List<AppCompatActivity?>> = MutableLiveData()
    val activities: List<AppCompatActivity?> get() = _activities.values.mapNotNull { it.get() }
    private val _activities: LinkedHashMap<String, WeakReference<AppCompatActivity?>> = linkedMapOf()

    private val Activity.tag: String get() = javaClass.simpleName

    fun init() {
        Log("init()")
        app.registerActivityLifecycleCallbacks(this)
    }

    @Suppress("unused")
    fun destroy() {
        Log("destroy()")
        app.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log("onActivityCreated(): $activity")
        if (activity !is AppCompatActivity) return
        _activities[activity.tag] = WeakReference(activity)
        updateLiveDataList()
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivityStarted(activity: Activity) {
        Log("onActivityStarted(): $activity")
        if (activity !is AppCompatActivity) return
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivityResumed(activity: Activity) {
        Log("onActivityResumed(): $activity")
        if (activity !is AppCompatActivity) return
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivityPaused(activity: Activity) {
        Log("onActivityPaused(): $activity")
        if (activity !is AppCompatActivity) return
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivityStopped(activity: Activity) {
        Log("onActivityStopped(): $activity")
        if (activity !is AppCompatActivity) return
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log("onActivityDestroyed(): $activity")
        if (activity !is AppCompatActivity) return
        _activities.remove(activity.tag)
        updateLiveDataList()
        updateForegroundActivity(
            activity
        )
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log("onActivitySaveInstanceState(): $activity, outState=$outState")
        if (activity !is AppCompatActivity) return
        updateForegroundActivity(
            activity
        )
    }

    private fun updateLiveDataList() {
        (activitiesLive as MutableLiveData).value =
            activities
    }

    private fun updateForegroundActivity(activity: AppCompatActivity) {
        val currentActivityLifecycle = foregroundActivity?.lifecycle?.currentState?.ordinal ?: -1
        val targetActivityLifeCycle = activity.lifecycle.currentState.ordinal

        if (targetActivityLifeCycle > currentActivityLifecycle) _foregroundActivity = _activities[activity.tag]
        (foregroundActivityLive as MutableLiveData).value = activity
    }

}

