package com.dm6801.framework.utilities

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class WeakRefHolder<in R, T>(_value: T?) : ReadWriteProperty<R, T?> {

    private val initialValue by lazy { WeakReference(_value) }
    var weakRef: WeakReference<T?>? = null; private set

    override operator fun getValue(thisRef: R, property: KProperty<*>): T? {
        return try {
            weakRef?.get() ?: initialValue.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override operator fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        try {
            weakRef =
                if (value != null) {
                    WeakReference(value)
                } else {
                    weakRef?.clear()
                    null
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

fun <R, T> weakRef(value: T) =
    WeakRefHolder<R, T>(value)
