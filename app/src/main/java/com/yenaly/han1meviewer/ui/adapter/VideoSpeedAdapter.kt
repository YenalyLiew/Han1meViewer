package com.yenaly.han1meviewer.ui.adapter

import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yenaly.han1meviewer.ui.view.CustomJzvdStd

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/09/24 024 14:51
 */
class VideoSpeedAdapter(private var currentIndex: Int) : BaseQuickAdapter<String, BaseViewHolder>(
    android.R.layout.simple_list_item_1,
    CustomJzvdStd.speedStringArray.toMutableList()
) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(android.R.id.text1, item)
        holder.setTextColor(
            android.R.id.text1,
            if (currentIndex == holder.bindingAdapterPosition) Color.parseColor("#fff85959")
            else Color.parseColor("#ffffff")
        )
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        viewHolder.getView<TextView>(android.R.id.text1).gravity = Gravity.CENTER
    }
}