package com.dm6801.framework.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*
import com.dm6801.framework.utilities.weakRef

abstract class RecyclerAdapter<VH : RecyclerAdapter<VH, T>.ViewHolder<T>, T : RecyclerAdapter.Identity<*>>(
    protected open val viewHolderClass: Class<*>? = null,
    asyncDifferConfig: AsyncDifferConfig<T>? = null,
    asyncListDiffer: AsyncListDiffer<T>? = null
) : RecyclerView.Adapter<VH>() {

    companion object {
        const val NO_ID = -1
    }

    abstract val layout: Int
    protected var recyclerView: RecyclerView? by weakRef(null)
    protected val viewHolderClasses: MutableMap<Int, Class<ViewHolder<*>>> = mutableMapOf()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    open val currentList: List<T>?
        get() {
            return try {
                asyncListDiffer.currentList
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }

    val asyncDifferConfig: AsyncDifferConfig<T> =
        asyncDifferConfig ?: AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem === newItem
        }).build()

    open val asyncListDiffer: AsyncListDiffer<T> =
        asyncListDiffer ?: AsyncListDiffer(object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                notifyItemRangeChanged(position, count, payload)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

        }, this.asyncDifferConfig)

    fun submitList(list: List<T>?, callback: (() -> Unit)? = null) {
        asyncListDiffer.submitList(list, callback)
    }

    fun clearList(callback: (() -> Unit)? = null) = submitList(null, callback)

    override fun getItemCount(): Int = currentList?.size ?: 0

    protected fun getItem(position: Int): T? {
        return try {
            val size = itemCount
            if (size == 0) throw IndexOutOfBoundsException("empty list")
            if (position in 0 until size)
                currentList?.get(position) ?: throw IndexOutOfBoundsException("empty list")
            else
                throw IndexOutOfBoundsException("position=$position, size=$size")
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.idLong ?: NO_ID.toLong()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(getLayoutByType(viewType), parent, false)
            viewHolderClass?.getDeclaredConstructor(javaClass, View::class.java)
                ?.newInstance(this, view) as VH
        } catch (t: Exception) {
            recyclerView?.context?.let(::View)
                ?.let { object : RecyclerView.ViewHolder(it) {} } as? VH
                ?: throw UnsupportedOperationException(t)
        }
    }

    @LayoutRes
    open fun getLayoutByType(viewType: Int): Int {
        return layout
    }

    open fun getViewHolderClassByType(viewType: Int): Class<ViewHolder<*>>? {
        return viewHolderClasses[viewType]
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        internalOnBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        internalOnBindViewHolder(holder, position, payloads)
    }

    protected open fun internalOnBindViewHolder(
        holder: VH,
        position: Int,
        payloads: MutableList<Any>? = null
    ) {
        getItem(position)?.let {
            holder.bind(it, position, payloads)
        }
    }

    interface Identity<T> : Comparable<T> {
        val id: T
        val idLong: Long
            get() {
                return try {
                    when (val id = this.id) {
                        is Long -> id
                        is Int -> id.toLong()
                        is Double -> id.toLong()
                        is Float -> id.toLong()
                        is String -> id.toLongOrNull()
                        else -> null
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    null
                } ?: hashCode().toLong()
            }
    }

    open inner class ViewHolder<T : Identity<*>>(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        open fun bind(item: T, position: Int, payloads: MutableList<Any>? = null) {}
    }

}

@Suppress("UNCHECKED_CAST")
fun <T: RecyclerView.Adapter<*>> RecyclerView.getTypedAdapter(): T? = adapter as? T