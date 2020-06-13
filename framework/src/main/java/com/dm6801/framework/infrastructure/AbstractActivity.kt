@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST")

package com.dm6801.framework.infrastructure

import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.dm6801.framework.*
import com.dm6801.framework.ui.ProgressBarStyled
import com.dm6801.framework.ui.hideKeyboard
import com.dm6801.framework.utilities.Log
import com.dm6801.framework.utilities.catch
import com.dm6801.framework.utilities.delay
import com.dm6801.framework.utilities.main
import kotlin.random.Random

fun showProgressBar(isContent: Boolean = false, isBlocking: Boolean = true) {
    foregroundActivity?.showProgressBar(isContent, isBlocking)
}

fun hideProgressBar() {
    foregroundActivity?.hideProgressBar()
}

fun requestPermissions(vararg permissions: String) {
    foregroundActivity?.requestPermissions(permissions as Array<String>)
}

fun ensurePermissions(vararg pairs: Pair<String, (() -> Unit)?>) {
    foregroundActivity?.ensurePermissions(pairs.toMap())
}

fun isPermissionGranted(permission: String): Boolean {
    return foregroundActivity?.isPermissionGranted(permission) ?: false
}

fun arePermissionsGranted(vararg permissions: String): Boolean {
    return permissions.all { isPermissionGranted(it) }
}

fun <T : Fragment> isFragmentAttached(clazz: Class<T>): Boolean {
    return AbstractApplication.activity?.supportFragmentManager?.fragments?.map { it.javaClass }
        ?.contains<Class<*>>(
            clazz
        ) ?: false
}

inline fun <reified T : Fragment> isFragmentAttached(): Boolean {
    return isFragmentAttached(T::class.java)
}

abstract class AbstractActivity : AppCompatActivity() {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = Random.nextInt(65535)
        val foregroundActivity: AbstractActivity? get() = AbstractApplication.activity as? AbstractActivity
    }

    abstract val layout: Int
    val Fragment.TAG get() = javaClass.simpleName
    open val fragmentContainer: Int = -1
    private val fragment: Fragment? get() = getFragments().lastOrNull()
    protected open val onlyTypedFragments = true

    val contentView: ViewGroup? get() = findViewById(android.R.id.content)
    open val landingClass: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        addGlobalFocusChangeListener()
        setBackStackListener()
        openLanding()
    }

    override fun onResume() {
        super.onResume()
        AbstractDialog.onResume()
    }

    override fun onStop() {
        super.onStop()
        currentFocus?.clearFocus()
        AbstractDialog.closeAll()
    }

    private fun addGlobalFocusChangeListener() {
        contentView?.viewTreeObserver?.addOnGlobalFocusChangeListener { _, newFocus ->
            if (newFocus !is EditText) hideKeyboard()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return foregroundFragment?.dispatchTouchEvent(ev)
            ?: super.dispatchTouchEvent(ev)
    }

    //region permissions
    private val pendingPermissions: MutableMap<String, (() -> Unit?)?> = mutableMapOf()

    fun isPermissionGranted(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(this, permission) ==
                PermissionChecker.PERMISSION_GRANTED
    }

    fun ensurePermissions(list: Map<String, (() -> Unit?)?>) {
        val granted = mutableMapOf<String, (() -> Unit?)?>()

        for ((permission, callback) in list) {
            if (isPermissionGranted(permission))
                granted[permission] = callback
            else
                pendingPermissions[permission] = callback
        }

        if (pendingPermissions.isNotEmpty()) requestPermissions(pendingPermissions.keys.toTypedArray())
        granted.values.forEach { it?.invoke() }
    }

    fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val granted = mutableListOf<String>()
            val denied = mutableListOf<String>()

            for (i in grantResults.indices) {
                val permission = permissions[i]
                val result = grantResults[i]

                if (result == PermissionChecker.PERMISSION_GRANTED) granted.add(permission)
                else denied.add(permission)
            }

            onPermissionsDenied(*denied.toTypedArray())
            onPermissionsGranted(*granted.toTypedArray())
        }
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onPermissionsGranted(vararg permissions: String) {
        permissions.forEach { permission ->
            pendingPermissions.remove(permission)?.invoke()
        }
    }

    private fun onPermissionsDenied(vararg permissions: String) {
        permissions.forEach { permission ->
            pendingPermissions.remove(permission)
        }
    }
    //endregion

    //region fragments
    private val backStack: List<FragmentManager.BackStackEntry>
        get() {
            return if (supportFragmentManager.backStackEntryCount > 0)
                (0 until supportFragmentManager.backStackEntryCount).map {
                    supportFragmentManager.getBackStackEntryAt(it)
                }
            else
                emptyList()
        }
    val backStackNames: List<String> get() = backStack.mapNotNull { it.name }
    val backStackArguments: MutableMap<String, Map<String, Any?>> = mutableMapOf()
    val isLastFragment: Boolean
        get() = supportFragmentManager.backStackEntryCount <= 1 //|| getFragments().size == 1

    fun getFragments(): List<Fragment> {
        return if (onlyTypedFragments) supportFragmentManager.fragments.filterIsInstance<AbstractFragment>()
        else supportFragmentManager.fragments
    }

    private fun setBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            this@AbstractActivity.Log("fragmentManager: OnBackStackChanged(): stack=${getFragments().map { it.TAG }}")
            val foregroundFragment = foregroundFragment
            getFragments().forEach { fragment ->
                if (fragment !is AbstractFragment) return@forEach
                if (fragment == foregroundFragment) {
                    backStackArguments.remove(fragment.TAG)?.let(fragment::onArguments)
                    fragment.onForeground()
                }
                fragment.onBackground()
            }
        }
    }

    private fun openLanding() {
        landingClass
            ?.takeIf { (AbstractFragment::class.java.isAssignableFrom(it)) }
            ?.let { open(it.newInstance() as Fragment) }
    }

    fun popBackStack(flag: Int, tag: String? = null) {
        try {
            if (!supportFragmentManager.popBackStackImmediate(tag, flag)) {
                supportFragmentManager.popBackStack(tag, flag)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                supportFragmentManager.popBackStack(tag, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun popBackStack() {
        popBackStack(0, null)
    }

    fun open(
        fragment: Fragment,
        arguments: Map<String, Any?>? = null,
        replace: Boolean = false,
        addToBackStack: Boolean = true,
        hideProgressBar: Boolean = true
    ) = catch {
        when {
            backStackNames.contains(fragment.TAG) -> {
                popBackStack(FragmentManager.POP_BACK_STACK_INCLUSIVE, fragment.TAG)
                add(
                    fragment,
                    arguments,
                    addToBackStack = addToBackStack,
                    animate = true,
                    hideProgressBar = hideProgressBar
                )
            }
            else -> {
                if (replace)
                    replace(
                        fragment,
                        arguments,
                        addToBackStack = addToBackStack,
                        animate = true,
                        hideProgressBar = hideProgressBar
                    )
                else
                    add(
                        fragment,
                        arguments,
                        addToBackStack = addToBackStack,
                        animate = true,
                        hideProgressBar = hideProgressBar
                    )
            }
        }
    }

    fun replace(
        fragment: Fragment,
        arguments: Map<String, Any?>? = null,
        addToBackStack: Boolean = true,
        animate: Boolean = true,
        hideProgressBar: Boolean = true
    ) {
        arguments?.let { backStackArguments[fragment.TAG] = it }
        commit(
            supportFragmentManager,
            fragment,
            addToBackStack,
            animate,
            hideProgressBar
        ) {
            this@AbstractActivity.Log("fragmentManager: replace(): ${it.TAG}")
            replace(fragmentContainer, it, it.TAG)
        }
    }

    fun add(
        fragment: Fragment,
        arguments: Map<String, Any?>? = null,
        addToBackStack: Boolean = true,
        animate: Boolean = true,
        hideProgressBar: Boolean = true
    ) {
        arguments?.let { backStackArguments[fragment.TAG] = it }
        commit(
            supportFragmentManager,
            fragment,
            addToBackStack,
            animate,
            hideProgressBar
        ) {
            this@AbstractActivity.Log("fragmentManager: add(): ${it.TAG}")
            add(fragmentContainer, it, it.TAG)
        }
    }

    @Suppress("SameParameterValue")
    private fun commit(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        addToBackStack: Boolean,
        animate: Boolean,
        hideProgressBar: Boolean,
        action: FragmentTransaction.(Fragment) -> Unit
    ) = catch {
        fragmentManager.commit {
            if (animate) setCustomAnimations(
                R.anim.fragment_fade_enter,
                R.anim.fragment_fade_exit,
                R.anim.fragment_fade_enter,
                R.anim.fragment_fade_exit
            )
            if (addToBackStack) addToBackStack(fragment.TAG)
            else delay(1_000) {
                this@AbstractActivity.Log(
                    "fragmentManager: stack=${getFragments().map { it.TAG }}"
                )
            }
            catch { action(this, fragment) }
        }
        hideKeyboard()
        if (hideProgressBar) hideProgressBar()
    }

    open fun navigateBack(
        vararg args: Pair<String, Any?>,
        tag: String? = null,
        inclusive: Boolean = false
    ): Unit = catch {
        Log("fragmentManager: navigateBack()")
        when {
            tag != null -> {
                val index = backStackNames.indexOf(tag)
                when {
                    index == -1 -> return@catch
                    index == 0 && inclusive -> finish()
                    else -> {
                        takeArguments(index, args.toMap())
                        Log("fragmentManager: popBackStack($tag, ${if (inclusive) "POP_BACK_STACK_INCLUSIVE" else "0"})")
                        popBackStack(
                            if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0,
                            tag
                        )
                        hideKeyboard()
                        hideProgressBar()
                    }
                }
            }
            isLastFragment -> finish()
            else -> {
                takeArguments(backStackNames.lastIndex, args.toMap())
                Log("fragmentManager: popBackStack()")
                supportFragmentManager.popBackStack()
                hideKeyboard()
                hideProgressBar()
            }
        }
    } ?: Unit

    private fun takeArguments(index: Int, arguments: Map<String, Any?>?) {
        (arguments?.takeIf { it.isNotEmpty() }
            ?.toMap() as? Map<String, Any?>)?.let { map ->
            backStackNames.getOrNull(index - 1)?.let { previousTag ->
                backStackArguments[previousTag] = map
            }
        }
    }

    inline fun <reified T : AbstractFragment> navigateBack(
        vararg args: Pair<String, Any?>,
        inclusive: Boolean = false,
        a: Boolean = false
    ) {
        navigateBack(*args, tag = T::class.java.simpleName, inclusive = inclusive)
    }

    fun clearBackStack() {
        popBackStack(FragmentManager.POP_BACK_STACK_INCLUSIVE, null)
    }

    override fun onBackPressed() {
        if ((fragment as? AbstractFragment)?.onBackPressed() != true)
            navigateBack()
    }
    //endregion

    //region progress bar
    var isProgressBarAllowed: Boolean = true
    private var progressBar: ProgressBarStyled? = null

    fun refreshProgressBar() = main {
        progressBar?.show()
    }

    fun showProgressBar(isContent: Boolean = false, isBlocking: Boolean = true) = main {
        if (!isProgressBarAllowed) return@main
        if (progressBar != null || progressBar?.isContentLoading != isContent || progressBar?.isBlocking != isBlocking) {
            progressBar?.hide()
            progressBar = null
        }
        progressBar = createProgressBar(isContent, isBlocking)
        progressBar?.show()
    }

    protected open fun createProgressBar(
        isContent: Boolean,
        isBlocking: Boolean
    ): ProgressBarStyled {
        return ProgressBarStyled(
            this,
            root = contentView,
            isContentLoading = isContent,
            isBlocking = isBlocking
        )
    }

    fun hideProgressBar() = main {
        progressBar?.hide()
        progressBar = null
    }
    //endregion

}