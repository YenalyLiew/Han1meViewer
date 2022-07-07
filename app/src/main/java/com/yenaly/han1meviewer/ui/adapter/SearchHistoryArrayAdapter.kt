package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/23 023 13:16
 */
class SearchHistoryArrayAdapter(mContext: Context, val list: MutableList<String>) :
    ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, list) {

    private var clickAction: ((View, Int) -> Unit)? = null
    private var longClickAction: ((View, Int) -> Boolean)? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false).apply {

                findViewById<TextView>(android.R.id.text1).text = getItem(position) as String

                this.setOnClickListener {
                    clickAction?.invoke(it, position)
                }
                this.setOnLongClickListener {
                    longClickAction?.invoke(it, position) ?: false
                }
            }
    }

    fun setOnItemClickListener(action: (View, Int) -> Unit) {
        this.clickAction = action
    }

    fun setOnItemLongClickListener(action: (View, Int) -> Boolean) {
        this.longClickAction = action
    }
}