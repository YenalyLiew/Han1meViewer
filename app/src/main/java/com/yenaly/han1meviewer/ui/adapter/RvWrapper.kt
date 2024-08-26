package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.minusAssign
import androidx.core.view.plusAssign
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.yenaly.han1meviewer.R

/**
 * RecyclerView Wrapper，为了让多 LayoutManager 布局能够在 ConcatAdapter 中使用。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/27 027 17:00
 */
class RvWrapper<VH : ViewHolder>(
    private val adapter: RecyclerView.Adapter<VH>,
    private val customRv: ((Context) -> RecyclerView)?,
    private val layoutManager: () -> LayoutManager,
) : BaseSingleItemAdapter<Unit, QuickViewHolder>() {
    companion object {
        fun <VH : ViewHolder> RecyclerView.Adapter<VH>.wrappedWith(
            customRv: ((Context) -> RecyclerView)? = null,
            layoutManager: () -> LayoutManager,
        ) = RvWrapper(this, customRv, layoutManager)
    }

    var wrapper: RecyclerView? = null
        private set

    private var onWrap: (RecyclerView.() -> Unit)? = null

    fun doOnWrap(block: RecyclerView.() -> Unit) {
        onWrap = block
    }

    override fun onBindViewHolder(holder: QuickViewHolder, item: Unit?) = Unit

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_rv_wrapper, parent).also { viewHolder ->
            val frame = viewHolder.itemView as ViewGroup
            var rv = this@RvWrapper.customRv?.invoke(context)?.apply {
                id = R.id.rv
                isNestedScrollingEnabled = false
            }
            val prevRv = viewHolder.getView<RecyclerView>(R.id.rv)
            if (rv != null) {
                frame -= prevRv
                rv.layoutParams = prevRv.layoutParams
                frame += rv
            } else {
                rv = prevRv
            }
            rv.apply wr@{
                this@wr.layoutManager = this@RvWrapper.layoutManager()
                this@wr.adapter = this@RvWrapper.adapter
                this@RvWrapper.wrapper = this@wr
                onWrap?.invoke(this@wr)
            }
        }
    }
}