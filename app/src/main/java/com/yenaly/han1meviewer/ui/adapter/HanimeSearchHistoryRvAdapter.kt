package com.yenaly.han1meviewer.ui.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/11 011 20:57
 */
class HanimeSearchHistoryRvAdapter :
    BaseQuickAdapter<SearchHistoryEntity, HanimeSearchHistoryRvAdapter.ViewHolder>(R.layout.item_search_history) {

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<SearchHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: SearchHistoryEntity,
                newItem: SearchHistoryEntity,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SearchHistoryEntity,
                newItem: SearchHistoryEntity,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    var listener: OnItemViewClickListener? = null

    inner class ViewHolder(view: View) : BaseViewHolder(view) {
        val tv = getView<TextView>(R.id.tv_text)
        val btn = getView<Button>(R.id.btn_remove)
        val root = getView<View>(R.id.root)
    }

    override fun convert(holder: ViewHolder, item: SearchHistoryEntity) {
        holder.tv.text = item.query
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.btn.setOnClickListener {
            listener?.onItemRemoveListener(it, getItem(viewHolder.bindingAdapterPosition))
        }
        viewHolder.root.setOnClickListener {
            listener?.onItemClickListener(it, getItem(viewHolder.bindingAdapterPosition))
        }
    }

    interface OnItemViewClickListener {
        fun onItemClickListener(v: View, history: SearchHistoryEntity)
        fun onItemRemoveListener(v: View, history: SearchHistoryEntity)
    }
}