package com.yenaly.han1meviewer.ui.view.funcbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.google.android.material.button.MaterialButton
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.logFieldsChange

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @since 2025/3/11 22:05
 */
class Hanidapter : BaseDifferAdapter<Hanidokitem, QuickViewHolder>(Hanidiff) {

    companion object {
        const val ICON = 1 shl 0
        const val TEXT = 1 shl 1
        const val BACK = 1 shl 2
    }

    val hanidontroller = Hanidontroller()

    private object Hanidiff : DiffUtil.ItemCallback<Hanidokitem>() {

        private const val TAG = "Hanidiff"

        override fun areItemsTheSame(oldItem: Hanidokitem, newItem: Hanidokitem): Boolean {
            return oldItem contentEquals newItem
        }

        override fun areContentsTheSame(oldItem: Hanidokitem, newItem: Hanidokitem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Hanidokitem, newItem: Hanidokitem): Int {
            logFieldsChange(TAG, oldItem, newItem)
            var mask = 0
            if (oldItem.icon != newItem.icon) {
                mask = mask or ICON
            }
            if (oldItem.text != newItem.text) {
                mask = mask or TEXT
            }
            if (oldItem.isBack != newItem.isBack) {
                mask = mask or BACK
            }
            return mask
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Hanidokitem?) {
        item ?: return
        val button = holder.itemView as MaterialButton
        if (item.isBack) {
            button.setIconResource(R.drawable.ic_baseline_arrow_back_24)
            TooltipCompat.setTooltipText(button, context.getString(R.string.back))
        } else {
            button.setIconResource(item.icon)
            TooltipCompat.setTooltipText(button, context.getString(item.text))
        }
        if (item.text != 0) {
            button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
        } else {
            button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        }
        val action = item.viewAction
        button.setOnClickListener { view ->
            if (item.isBack) {
                if (hanidontroller.onBackPressed()) {
                    items = hanidontroller.currentHanidokitems
                }
            } else if (hanidontroller.onItemClicked(item)) {
                items = hanidontroller.currentHanidokitems
            }
            if (action != null && !item.isBack) {
                action.onClick(view)
            }
        }

    }

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: Hanidokitem?,
        payloads: List<Any>
    ) {
        item ?: return
        if (payloads.isEmpty() || payloads.first() == 0) {
            return super.onBindViewHolder(holder, position, item, payloads)
        }
        val button = holder.itemView as MaterialButton
        val mask = payloads.first() as Int
        if (mask and ICON != 0) {
            button.setIconResource(item.icon)
        }
        if (mask and TEXT != 0) {
            TooltipCompat.setTooltipText(button, context.getString(item.text))
            if (item.text != 0) {
                button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
            } else {
                button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            }
        }
        if (mask and BACK != 0) {
            if (item.isBack) {
                button.setIconResource(R.drawable.ic_baseline_arrow_back_24)
                TooltipCompat.setTooltipText(button, context.getString(R.string.back))
            } else {
                button.setIconResource(item.icon)
                TooltipCompat.setTooltipText(button, context.getString(item.text))
            }
            val action = item.viewAction
            button.setOnClickListener { view ->
                if (item.isBack) {
                    if (hanidontroller.onBackPressed()) {
                        items = hanidontroller.currentHanidokitems
                    }
                } else if (hanidontroller.onItemClicked(item)) {
                    items = hanidontroller.currentHanidokitems
                }
                if (action != null && !item.isBack) {
                    action.onClick(view)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        // MaterialButton 构成
        val view = View.inflate(context, R.layout.item_hanidock, null)
        return QuickViewHolder(view)
    }
}