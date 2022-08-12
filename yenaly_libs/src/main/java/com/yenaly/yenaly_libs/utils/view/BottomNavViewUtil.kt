@file:JvmName("BottomNavViewUtil")

package com.yenaly.yenaly_libs.utils.view

import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 解决比如ViewPager2的Fragment内滑动无法传递给Activity的[BottomNavigationView]
 * 的[HideBottomViewOnScrollBehavior]问题，
 * 通过这种方法可以使BNV随[view]的滑动而隐藏或显示
 *
 * @param view 比如ViewPager2之类
 * @param scrollToHide 是否选择滑动隐藏
 */
fun BottomNavigationView.toggleBottomNavBehavior(view: View, scrollToHide: Boolean) {
    val layoutParams = this.layoutParams as? CoordinatorLayout.LayoutParams
        ?: throw IllegalStateException("parent needs to be coordinator layout!")
    val scrollBehavior = YenalyHideBottomViewOnScrollBehavior<BottomNavigationView>()
    layoutParams.behavior = scrollBehavior
    val behavior = layoutParams.behavior as YenalyHideBottomViewOnScrollBehavior
    behavior.slideUp(this)
    if (scrollToHide) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(leftMargin, topMargin, rightMargin, 0)
        }
    } else {
        layoutParams.behavior = null
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(
                leftMargin, topMargin, rightMargin,
                this@toggleBottomNavBehavior.measuredHeight + layoutParams.bottomMargin
            )
        }
    }
}