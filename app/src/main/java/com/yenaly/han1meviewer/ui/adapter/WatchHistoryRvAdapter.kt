package com.yenaly.han1meviewer.ui.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.DATE_FORMAT
import com.yenaly.han1meviewer.DATE_TIME_FORMAT
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemWatchHistoryBinding
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.toVideoCode
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.*

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/02 002 13:08
 */
class WatchHistoryRvAdapter :
    BaseQuickAdapter<WatchHistoryEntity, WatchHistoryRvAdapter.ViewHolder>(R.layout.item_watch_history) {

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemWatchHistoryBinding>(view) {
        val binding = dataBinding!!
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<WatchHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: WatchHistoryEntity,
                newItem: WatchHistoryEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: WatchHistoryEntity,
                newItem: WatchHistoryEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun convert(holder: ViewHolder, item: WatchHistoryEntity) {
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvAddedTime.text = TimeUtil.millis2String(item.watchDate, DATE_TIME_FORMAT)
        holder.binding.tvReleaseDate.text = TimeUtil.millis2String(item.releaseDate, DATE_FORMAT)
        holder.binding.tvTitle.text = item.title
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.itemView.apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                val videoCode = item.redirectLink.toVideoCode()
                context.activity?.startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
            setOnLongClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                copyTextToClipboard("${item.title}\n${item.redirectLink}")
                showShortToast(R.string.copy_to_clipboard)
                return@setOnLongClickListener true
            }
        }
    }
}