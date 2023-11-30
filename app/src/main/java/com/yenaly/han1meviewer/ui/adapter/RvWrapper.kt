package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.yenaly.han1meviewer.R
import kotlin.math.abs

/**
 * RecyclerView Wrapper，为了让多 LayoutManager 布局能够在 ConcatAdapter 中使用。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/27 027 17:00
 */
class RvWrapper<VH : ViewHolder>(
    private val adapter: RecyclerView.Adapter<VH>,
    private val layoutManager: () -> LayoutManager,
) : BaseSingleItemAdapter<Unit, QuickViewHolder>() {
    companion object {
        fun <VH : ViewHolder> RecyclerView.Adapter<VH>.wrappedWith(
            layoutManager: () -> LayoutManager,
        ) = RvWrapper(this, layoutManager)
    }

    var wrapper: RecyclerView? = null
        private set

    override fun onBindViewHolder(holder: QuickViewHolder, item: Unit?) = Unit

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_rv_wrapper, parent).also { viewHolder ->
            viewHolder.getView<RecyclerView>(R.id.rv).apply {
                wrapper = this
                layoutManager = this@RvWrapper.layoutManager()
                adapter = this@RvWrapper.adapter
            }
        }
    }

    class RecyclerViewInWrapper : RecyclerView {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context, attrs, defStyleAttr
        )

        private var disallowIntercept = false

        private var startX = 0
        private var startY = 0

        override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x.toInt()
                    startY = ev.y.toInt()
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    val endX = ev.x.toInt()
                    val endY = ev.y.toInt()
                    val disX = abs(endX - startX)
                    val disY = abs(endY - startY)
                    if (disX > disY) {
                        //为了解决RecyclerView嵌套RecyclerView时横向滑动的问题
                        if (disallowIntercept) {
                            parent.requestDisallowInterceptTouchEvent(disallowIntercept)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(canScrollHorizontally(startX - endX))
                        }
                    } else {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
            return super.dispatchTouchEvent(ev)
        }

    }
}