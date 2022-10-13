package com.yenaly.han1meviewer.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadedBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadedEntity
import com.yenaly.han1meviewer.util.openDownloadedHanimeVideoLocally
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.yenaly_libs.utils.TimeUtil
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.formatFileSize
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 12:31
 */
class HanimeDownloadedRvAdapter(private val fragment: DownloadedFragment) :
    BaseQuickAdapter<HanimeDownloadedEntity, HanimeDownloadedRvAdapter.ViewHolder>(R.layout.item_hanime_downloaded) {

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemHanimeDownloadedBinding>(view) {
        val binding = dataBinding!!
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HanimeDownloadedEntity>() {
            override fun areContentsTheSame(
                oldItem: HanimeDownloadedEntity,
                newItem: HanimeDownloadedEntity
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: HanimeDownloadedEntity,
                newItem: HanimeDownloadedEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: ViewHolder, item: HanimeDownloadedEntity) {
        holder.binding.tvTitle.text = item.title
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvAddedTime.text = TimeUtil.millis2String(item.addDate, "yyyy-MM-dd HH:mm")
        holder.binding.tvReleaseDate.text = TimeUtil.millis2String(item.releaseDate, "yyyy-MM-dd")
        holder.binding.tvQuality.text = spannable {
            item.quality.text()
            " | ".span {
                color(Color.RED)
            }
            item.videoUri.toUri().toFile().length().formatFileSize().text()
        }
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            context.activity?.startActivity<VideoActivity>(VIDEO_CODE to item.videoCode)
        }
        viewHolder.binding.btnDelete.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            val file = item.videoUri.toUri().toFile()
            MaterialAlertDialogBuilder(context)
                .setTitle("你確定要刪除嗎？")
                .setMessage("你現在正要準備刪除" + "\n" + file.name)
                .setPositiveButton("沒錯") { _, _ ->
                    if (file.exists()) file.delete()
                    fragment.viewModel.deleteDownloadedHanimeByVideoCode(item.videoCode)
                }
                .setNegativeButton("算了", null)
                .show()
        }
        viewHolder.binding.btnLocalPlayback.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            context.openDownloadedHanimeVideoLocally(item.videoUri)
        }
    }
}