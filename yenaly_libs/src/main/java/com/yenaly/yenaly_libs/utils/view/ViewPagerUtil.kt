@file:JvmName("ViewPagerUtil")

package com.yenaly.yenaly_libs.utils.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

typealias NewFragment = () -> Fragment

/**
 * 如果直接给ViewPager2设置overScrollMode会无效，
 * 这种方式能设置有效的overScrollMode
 */
inline var ViewPager2.realOverScrollMode: Int
    get() {
        val vpChild = this.getChildAt(0)
        if (vpChild is RecyclerView) {
            return vpChild.overScrollMode
        }
        // should not reach here.
        return -1
    }
    set(value) {
        val vpChild = this.getChildAt(0)
        if (vpChild is RecyclerView) {
            vpChild.overScrollMode = value
        }
    }

/**
 * ViewPager2快速設置FragmentStateAdapter，這裏作用域為FragmentActivity
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ViewPager2.setUpFragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    crossinline addAction: SimpleFragmentStateAdapter.() -> Unit
) {
    adapter = SimpleFragmentStateAdapter(fragmentActivity).apply(addAction)
}

/**
 * ViewPager2快速設置FragmentStateAdapter，這裏作用域為Fragment
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ViewPager2.setUpFragmentStateAdapter(
    fragment: Fragment,
    crossinline addAction: SimpleFragmentStateAdapter.() -> Unit
) {
    adapter = SimpleFragmentStateAdapter(fragment).apply(addAction)
}