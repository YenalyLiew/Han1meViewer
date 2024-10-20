package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.google.android.material.checkbox.MaterialCheckBox
import com.yenaly.han1meviewer.logic.model.SearchOption

class HSearchTagAdapter : BaseQuickAdapter<SearchOption, QuickViewHolder>() {

    val checkedSet = mutableSetOf<SearchOption>()

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: SearchOption?) {
        item ?: return
        val chip = holder.itemView as MaterialCheckBox
        chip.isChecked = item in checkedSet
        chip.text = item.value
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        val checkBox = MaterialCheckBox(context).apply {
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }
        return QuickViewHolder(checkBox).apply {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                getItem(position)?.let { item ->
                    if (isChecked) {
                        checkedSet += item
                    } else {
                        checkedSet -= item
                    }
                }
            }
        }
    }
}