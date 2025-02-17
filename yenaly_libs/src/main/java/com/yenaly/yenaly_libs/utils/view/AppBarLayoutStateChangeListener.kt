package com.yenaly.yenaly_libs.utils.view

import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

fun AppBarLayout.offsetChanges(): Flow<Int> {
    return callbackFlow {
        val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            trySend(verticalOffset)
        }
        addOnOffsetChangedListener(listener)
        awaitClose { removeOnOffsetChangedListener(listener) }
    }
}

/**
 * 用于监听AppBarLayout是否展开或折叠
 *
 * 从[AppBarLayout.addOnOffsetChangedListener]中调用该类
 *
 * @author Yenaly Liew
 * @time 2022/06/01 001 16:38
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