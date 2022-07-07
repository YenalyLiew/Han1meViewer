package com.yenaly.yenaly_libs.utils.view

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

/**
 * 用于监听AppBarLayout是否展开或折叠
 *
 * 从[AppBarLayout.addOnOffsetChangedListener]中调用该类
 *
 * @ProjectName : BlViewer
 * @Author : Yenaly Liew
 * @Time : 2022/06/01 001 16:38
 * @Description : Description...
 */
abstract class AppBarLayoutStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    enum class State {
        EXPANDED, // 展开
        COLLAPSED, // 折叠
        INTERMEDIATE; // 中间态
    }

    private var mCurrentState: State = State.INTERMEDIATE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        when {
            verticalOffset == 0 -> {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED)
                }
                mCurrentState = State.EXPANDED
            }
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED)
                }
                mCurrentState = State.COLLAPSED
            }
            else -> {
                if (mCurrentState != State.INTERMEDIATE) {
                    onStateChanged(appBarLayout, State.INTERMEDIATE)
                }
                mCurrentState = State.INTERMEDIATE
            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State)
}