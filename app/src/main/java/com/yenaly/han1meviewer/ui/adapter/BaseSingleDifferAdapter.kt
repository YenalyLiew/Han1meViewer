package com.yenaly.han1meviewer.ui.adapter

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/04/05 005 20:44
 */
abstract class BaseSingleDifferAdapter<T : Any, VH : RecyclerView.ViewHolder> :
    BaseDifferAdapter<T, VH> {

    private constructor(config: AsyncDifferConfig<T>, items: List<T>) : super(config, items)

    constructor(diffCallback: DiffUtil.ItemCallback<T>) : this(
        AsyncDifferConfig.Builder(diffCallback).build(), emptyList()
    )

    constructor(diffCallback: DiffUtil.ItemCallback<T>, item: T) : this(
        AsyncDifferConfig.Builder(diffCallback).build(), listOf(item)
    )

    constructor(config: AsyncDifferConfig<T>) : this(config, emptyList())

    constructor(config: AsyncDifferConfig<T>, item: T) : this(config, listOf(item))

    var item: T?
        get() = items.firstOrNull()
        set(value) {
            items = if (value != null) listOf(value) else emptyList()
        }

    suspend fun submit(item: T?) = suspendCoroutine { cont ->
        val list = item?.let(::listOf).orEmpty()
        submitList(list) {
            cont.resume(Unit)
        }
    }

    protected abstract fun onBindViewHolder(holder: VH, item: T?)

    open fun onBindViewHolder(holder: VH, item: T?, payloads: List<Any>) {
        onBindViewHolder(holder, item)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, item: T?) {
        onBindViewHolder(holder, item)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, item: T?, payloads: List<Any>) {
        onBindViewHolder(holder, item, payloads)
    }

    override fun add(data: T) {
        throw RuntimeException("Please use setItem()")
    }

    override fun add(position: Int, data: T) {
        throw RuntimeException("Please use setItem()")
    }

    override fun addAll(collection: Collection<T>) {
        throw RuntimeException("Please use setItem()")
    }

    override fun addAll(position: Int, collection: Collection<T>) {
        throw RuntimeException("Please use setItem()")
    }

    override fun remove(data: T) {
        throw RuntimeException("Please use setItem()")
    }

    override fun removeAtRange(range: IntRange) {
        throw RuntimeException("Please use setItem()")
    }

    override fun removeAt(position: Int) {
        throw RuntimeException("Please use setItem()")
    }

    override fun set(position: Int, data: T) {
        throw RuntimeException("Please use setItem()")
    }
}