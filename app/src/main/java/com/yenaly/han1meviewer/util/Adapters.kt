package com.yenaly.han1meviewer.util

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.chad.library.adapter4.BaseDifferAdapter
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.applicationContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 设置状态视图
 *
 * 必须在绑定 RV 后才能调用，因为用了 BaseQuickAdapter 里面的 context
 */
fun com.chad.library.adapter4.BaseQuickAdapter<*, *>.setStateViewLayout(
    @LayoutRes layoutRes: Int,
    text: String? = null,
) {
    val view = View.inflate(context, layoutRes, FrameLayout(context))
    view.findViewById<TextView>(R.id.tv_empty).text =
        text ?: context.getString(R.string.here_is_empty)
    stateView = view
}

/**
 * 设置状态视图
 */
fun com.chad.library.adapter4.BaseQuickAdapter<*, *>.setStateViewLayout(
    view: View,
    text: String? = null,
) {
    view.findViewById<TextView>(R.id.tv_empty).text =
        text ?: applicationContext.getString(R.string.here_is_empty)
    stateView = view
}

suspend fun <T : Any> BaseDifferAdapter<T, *>.awaitSubmitList(list: List<T>?) =
    suspendCoroutine { cont ->
        submitList(list) {
            cont.resume(Unit)
        }
    }

/**
 * BRVAH4 的 getItem() 现在开始会返回 null 值了，为了避免各种 null 检查，
 * 我们直接在这里加一个非空断言，这样就不用每次都检查了。
 * 而且一般情况下不会为 null
 */
@OptIn(ExperimentalContracts::class)
@Suppress("NOTHING_TO_INLINE")
@Deprecated("Use safe call instead, this can easily cause NPE.", ReplaceWith("this ?: return"))
inline fun <T> T?.notNull(): T {
    contract {
        returns() implies (this@notNull != null)
    }
    return checkNotNull(this)
}