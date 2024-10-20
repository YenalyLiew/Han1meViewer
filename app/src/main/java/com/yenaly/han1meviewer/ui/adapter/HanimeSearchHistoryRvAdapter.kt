package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/26 026 16:42
 */
class HanimeSearchHistoryRvAdapter :
    BaseDifferAdapter<SearchHistoryEntity, QuickViewHolder>(COMPARATOR) {

    init {
        isStateViewEnable = true
    }

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

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: SearchHistoryEntity?,
    ) {
        item ?: return
        holder.setText(R.id.tv_text, item.query)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_search_history, parent).also { viewHolder ->
            viewHolder.getView<View>(R.id.btn_remove).setOnClickListener {
                // #issue-142: 部分机型调用 getItem().notNull() 可能会报错
                listener?.onItemRemoveListener(
                    it, getItem(viewHolder.bindingAdapterPosition)
                )
            }
            viewHolder.getView<View>(R.id.root).setOnClickListener {
                listener?.onItemClickListener(
                    it, getItem(viewHolder.bindingAdapterPosition)
                )
            }
        }
    }

    interface OnItemViewClickListener {
        fun onItemClickListener(v: View, history: SearchHistoryEntity?)
        fun onItemRemoveListener(v: View, history: SearchHistoryEntity?)
    }
}