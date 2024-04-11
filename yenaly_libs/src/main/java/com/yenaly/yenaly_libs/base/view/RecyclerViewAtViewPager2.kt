package com.yenaly.yenaly_libs.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * 重写RecyclerView，解决横向RecyclerView和ViewPager2的滑动冲突。
 */
class RecyclerViewAtViewPager2 : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var disallowIntercept = false

    private var startX = 0
    private var startY = 0

    private var vpParent: ViewParent? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        vpParent = getVpParent()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
                vpParent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x.toInt()
                val endY = ev.y.toInt()
                val disX = abs(endX - startX)
                val disY = abs(endY - startY)
                if (disX > disY) {
                    //为了解决RecyclerView嵌套RecyclerView时横向滑动的问题
                    if (disallowIntercept) {
                        vpParent?.requestDisallowInterceptTouchEvent(disallowIntercept)
                    } else {
                        vpParent?.requestDisallowInterceptTouchEvent(canScrollHorizontally(startX - endX))
                    }
                } else {
                    vpParent?.requestDisallowInterceptTouchEvent(false)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                vpParent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        this.disallowIntercept = disallowIntercept
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun getVpParent(): ViewParent? {
        var p = this.parent
        while (p != null) {
            if (p is ViewPager2) {
                return p
            }
            p = p.parent
        }
        return null
    }
}