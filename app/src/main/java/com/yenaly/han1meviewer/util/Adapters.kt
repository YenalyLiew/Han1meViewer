package com.yenaly.han1meviewer.util

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.yenaly.han1meviewer.R

/**
 * 快速加載空視圖
 */
fun BaseQuickAdapter<*, *>.resetEmptyView(view: View, text: String? = null) {
    view.findViewById<TextView>(R.id.tv_empty).text =
        text ?: context.getString(R.string.here_is_empty)
    setEmptyView(view)
}