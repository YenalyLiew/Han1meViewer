package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import kotlin.math.abs

/**
 * 暂时是专门为影片界面中的横向功能滚动条而设计的
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/21 021 16:05
 */
class HorizontalNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : HorizontalScrollView(context, attrs) {

    private var disallowIntercept = false
    private var startX = 0
    private var startY = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
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
                    if (disallowIntercept) {
                        parent.requestDisallowInterceptTouchEvent(disallowIntercept)
                    } else {
                        // 防止划到下一页
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        this.disallowIntercept = disallowIntercept
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }
}