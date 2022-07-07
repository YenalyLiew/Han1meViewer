package com.yenaly.han1meviewer.ui.adapter

import android.app.Activity
import android.view.View
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemWatchHistoryBinding
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.toVideoCode
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.TimeUtil
import com.yenaly.yenaly_libs.utils.copyTextToClipboard
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity

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

    override fun convert(holder: ViewHolder, item: WatchHistoryEntity) {
        holder.binding.ivCover.load(item.coverUrl)
        holder.binding.tvAddedTime.text = TimeUtil.millis2String(item.watchDate, "yyyy-MM-dd HH:mm")
        holder.binding.tvReleaseDate.text = TimeUtil.millis2String(item.releaseDate, "yyyy-MM-dd")
        holder.binding.tvTitle.text = item.title
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.itemView.apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                val videoCode = item.redirectLink.toVideoCode()
                (context as Activity).startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
            setOnLongClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                copyTextToClipboard("${item.title}\n${item.redirectLink}")
                showShortToast(context.getString(R.string.copy_to_clipboard))
                return@setOnLongClickListener true
            }
        }
    }
}