package com.dm6801.framework.ui

import androidx.lifecycle.*
import com.dm6801.framework.infrastructure.AbstractFragment
import com.dm6801.framework.utilities.main
import com.dm6801.framework.utilities.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun ViewModel.safeLaunch(
    context: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
) = CoroutineScope(viewModelScope.coroutineContext + context).safeLaunch(block)

inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel() =
    ViewModelProvider(this)[T::class.java]

fun <T> LiveData<T>.set(value: T, post: Boolean = false): Boolean {
    if (this !is MutableLiveData) return false
    return try {
        if (post) postValue(value)
        else main { setValue(value) }
        true
    } catch (t: Throwable) {
        t.printStackTrace()
        false
    }
}