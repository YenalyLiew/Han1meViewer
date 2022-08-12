package com.yenaly.han1meviewer.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mancj.materialsearchbar.R
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/30 030 11:59
 */
class SearchHistoryRvAdapter(inflater: LayoutInflater) :
    SuggestionsAdapter<String, BaseViewHolder>(inflater) {

    private var listener: OnItemViewClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_last_request, parent, false)
        val viewHolder = BaseViewHolder(view)
        viewHolder.setTextColor(R.id.text, Color.WHITE)
        viewHolder.getView<View>(R.id.iv_delete).setOnClickListener {
            listener?.onItemDeleteListener(suggestions[viewHolder.bindingAdapterPosition], it)
        }
        viewHolder.itemView.setOnClickListener {
            listener?.onItemClickListener(suggestions[viewHolder.bindingAdapterPosition], it)
        }
        return viewHolder
    }

    override fun onBindSuggestionHolder(
        suggestion: String,
        holder: BaseViewHolder,
        position: Int
    ) {
        holder.setText(R.id.text, suggestion)
    }

    override fun getSingleViewHeight() = 50

    fun setListener(listener: OnItemViewClickListener) {
        this.listener = listener
    }

    interface OnItemViewClickListener {
        fun onItemClickListener(suggestion: String, v: View?)
        fun onItemDeleteListener(suggestion: String, v: View?)
    }
}