@file:Suppress("UNCHECKED_CAST", "PropertyName", "RedundantOverride", "unused")

package com.dm6801.framework.infrastructure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dm6801.framework.ui.clickable

abstract class AbstractFragment : Fragment() {

    abstract val layout: Int
    val TAG = javaClass.simpleName

    protected open val activity: AbstractActivity? get() = getActivity() as? AbstractActivity
    val isLastFragment: Boolean get() = activity?.isLastFragment ?: false

    open class Comp(_class: Class<*>? = null) {
        protected val clazz: Class<*>? = _class ?: this::class.java.enclosingClass

        open fun open(
            vararg args: Pair<String, Any?>,
            replace: Boolean = false,
            addToBackStack: Boolean = true,
            hideProgressBar: Boolean = true
        ) {
            if (clazz == null || !AbstractFragment::class.java.isAssignableFrom(clazz)) return
            val activity = foregroundActivity ?: return
            try {
                val fragment = clazz.newInstance() as AbstractFragment
                activity.open(
                    fragment,
                    args.takeIf { it.isNotEmpty() }?.toMap(),
                    replace,
                    addToBackStack,
                    hideProgressBar
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun close() {
            if (clazz == foregroundFragment?.javaClass)
                foregroundActivity?.popBackStack(
                    FragmentManager.POP_BACK_STACK_INCLUSIVE, foregroundFragment?.tag
                )
        }

        fun navigateBack(
            vararg args: Pair<String, Any?>,
            tag: String? = null,
            inclusive: Boolean = false
        ) {
            foregroundActivity?.navigateBack(*args, tag = tag, inclusive = inclusive)
        }

        inline fun <reified T : AbstractFragment> navigateBack(
            vararg args: Pair<String, Any?>,
            inclusive: Boolean = false,
            a: Boolean = false
        ) {
            navigateBack(*args, tag = T::class.java.simpleName, inclusive = inclusive)
        }

        fun backPress() {
            foregroundActivity?.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onArguments(activity?.backStackArguments?.remove(TAG) ?: emptyMap())
    }

    open fun onArguments(arguments: Map<String, Any?>) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout, container, false)
            .apply { clickable() }
    }

    override fun onResume() {
        super.onResume()
    }

    open fun onForeground() {}

    open fun onBackground() {}

    open fun onBackPressed(): Boolean {
        return false
    }

    open fun dispatchTouchEvent(ev: MotionEvent?): Boolean? {
        return null
    }

    open fun onSoftKeyboard(isVisible: Boolean) {}

}
