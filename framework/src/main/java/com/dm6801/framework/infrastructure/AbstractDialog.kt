package com.dm6801.framework.infrastructure

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import com.dm6801.framework.*
import com.dm6801.framework.ui.getScreenSize
import com.dm6801.framework.ui.isVisibleOnScreen
import com.dm6801.framework.ui.wasClicked
import com.dm6801.framework.utilities.Log
import com.dm6801.framework.utilities.weakRef
import java.lang.ref.WeakReference

abstract class AbstractDialog :
    Dialog(AbstractApplication.activity ?: AbstractApplication.instance) {

    companion object {
        val instances: Map<String, WeakReference<AbstractDialog?>?> = mutableMapOf()

        fun closeAll() {
            instances.values.forEach { instance ->
                instance?.get()?.apply {
                    cancel()
                    clearInstance()
                }
            }
        }

        fun onResume() {
            instances.values.forEach { instance ->
                instance?.get()?.onResume()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    open class Comp<T : AbstractDialog>(_class: Class<*>? = null) {
        val isOpen: Boolean get() = instance != null

        private val clazz: Class<T>? = (_class ?: this::class.java.enclosingClass) as? Class<T>
        private var instance: T?
            get() = _instance?.get()
            set(value) {
                _instance?.clear()
                _instance =
                    if (value != null) WeakReference(value)
                    else null
            }
        private var _instance: WeakReference<T?>? = null

        @Suppress("UNCHECKED_CAST")
        open fun open(vararg args: Pair<String, Any?>, refresh: Boolean = true): T? {
            return try {
                when (instance) {
                    null -> {
                        openNewInstance(args.toMap())
                    }
                    else -> {
                        if (refresh || (instance?.isShowing != true && !instance?.view.isVisibleOnScreen())) {
                            instance?.cancel()
                            openNewInstance(args.toMap())
                        } else {
                            instance?.apply { onArguments(args.toMap()) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun openNewInstance(args: Map<String, Any?>): T? {
            instance = newInstance(args.toMap())
            (instances as MutableMap)[clazz?.simpleName ?: return null] =
                _instance as? WeakReference<AbstractDialog?>
            instance?.show()
            return instance
        }

        @Suppress("UNCHECKED_CAST")
        private fun newInstance(args: Map<String, Any?>): T? {
            return try {
                clazz?.newInstance()?.apply { arguments = args }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun close() {
            instance?.cancel()
            instance = null
        }
    }

    protected var view: View? by weakRef(null)
    protected var arguments: Map<String, Any?> = mutableMapOf()
    protected abstract val layout: Int
    protected open val isCancelable: Boolean = true
    protected open val widthFactor: Float? = null
    protected open val heightFactor: Float? = null
    protected open val gravity: Int = Gravity.CENTER
    private var wasCanceled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.takeIf { it.isNotEmpty() }?.let(::onArguments)
        inflateView()
    }

    open fun onArguments(arguments: Map<String, Any?>) {
        Log("onArguments(): $arguments")
    }

    private fun inflateView() {
        if (widthFactor == null || heightFactor == null) {
            view = LayoutInflater.from(context)
                .inflate(layout, window?.decorView?.findViewById(android.R.id.content), true)
        } else {
            window?.attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = this@AbstractDialog.gravity
            }
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window?.setBackgroundDrawableResource(R.color.transparent)
            view = LayoutInflater.from(context).inflate(layout, null, false)?.also {
                setContentView(
                    it, ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            setDimensions()
        }
        view?.let(::onViewCreated)
            ?: run {
                dismiss()
                clearInstance()
                return
            }
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCancelable)
        setOnDismissListener {
            if (!wasCanceled) onDismiss()
            clearInstance()
        }
        setOnCancelListener {
            wasCanceled = true
            onCancel()
        }
        setOnKeyListener()
    }

    private fun setDimensions() {
        val widthFactor = widthFactor ?: return
        val heightFactor = heightFactor ?: return
        val (screenWidth, screenHeight) = getScreenSize()
        if (screenWidth == 0 || screenHeight == 0) return
        view?.layoutParams = FrameLayout.LayoutParams(
            (screenWidth * widthFactor).toInt(),
            (screenHeight * heightFactor).toInt()
        )
    }

    open fun onResume() {}

    open fun onCancel() {}

    open fun onDismiss() {}

    private fun setOnKeyListener() {
        setOnKeyListener { _, keyCode, event ->
            Log("$keyCode - $event")
            if (event.action == KeyEvent.ACTION_UP)
                when (keyCode) {
                    //KeyEvent.KEYCODE_HOME -> false
                    KeyEvent.KEYCODE_BACK -> {
                        onBackPressed()
                        true
                    }
                    else -> false
                }
            else false
        }
    }

    open fun onViewCreated(view: View) {}

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_UP) {
            if (view?.wasClicked(ev) != true && isCancelable) cancel()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (isCancelable) super.onBackPressed()
        else ownerActivity?.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        onStopped()
    }

    open fun onStopped() {
        dismiss()
        clearInstance()
    }

    private fun clearInstance() {
        (instances as MutableMap).remove(javaClass.simpleName)?.clear()
    }

}