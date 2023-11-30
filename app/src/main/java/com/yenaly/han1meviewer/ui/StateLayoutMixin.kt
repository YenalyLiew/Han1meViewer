package com.yenaly.han1meviewer.ui

import android.annotation.SuppressLint
import android.widget.TextView
import com.drake.statelayout.StateLayout
import com.yenaly.han1meviewer.R

/**
 * 用於初始化 StateLayout。
 *
 * 但是不是説實現了這個接口就代表有 StateLayout 了，有些我還是直接用的 BRVAH 自帶的 StateLayoutVH，
 * 因爲用 StateLayout 稍稍有點麻煩。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/30 030 15:24
 */
interface StateLayoutMixin {

    /**
     * 初始化 StateLayout
     */
    @SuppressLint("SetTextI18n")
    fun StateLayout.init(apply: StateLayout.() -> Unit = {}) {
        errorLayout = R.layout.layout_empty_view
        emptyLayout = R.layout.layout_empty_view
        onError {
            val err = it as Throwable
            findViewById<TextView>(R.id.tv_empty).text = "🥺\n${err.message}"
        }
        apply()
    }
}