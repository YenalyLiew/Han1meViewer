package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.LOCAL_DATE_TIME_FORMAT
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadedBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.han1meviewer.util.openDownloadedHanimeVideoLocally
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.formatFileSize
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/26 026 16:57
 */
class HanimeDownloadedRvAdapter(private val fragment: DownloadedFragment) :
    BaseDifferAdapter<HanimeDownloadEntity, DataBindingHolder<ItemHanimeDownloadedBinding>>(
        COMPARATOR
    ) {

    init {
        isStateViewEnable = true
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HanimeDownloadEntity>() {
            override fun areContentsTheSame(
                oldItem: HanimeDownloadEntity,
                newItem: HanimeDownloadEntity,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: HanimeDownloadEntity,
                newItem: HanimeDownloadEntity,
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemHanimeDownloadedBinding>,
        position: Int,
        item: HanimeDownloadEntity?,
    ) {
        item ?: return
        holder.binding.tvTitle.text = item.title
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvAddedTime.text =
            Instant.fromEpochMilliseconds(item.addDate).toLocalDateTime(
                TimeZone.currentSystemDefault()
            ).format(LOCAL_DATE_TIME_FORMAT)
        holder.binding.tvQuality.text = spannable {
            item.quality.text()
            " | ".span {
                color(Color.RED)
            }
            item.videoUri.toUri().toFile().length().formatFileSize().text()
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): DataBindingHolder<ItemHanimeDownloadedBinding> {
        return DataBindingHolder(
            ItemHanimeDownloadedBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        ).also { viewHolder ->
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener
                context.activity?.startActivity<VideoActivity>(VIDEO_CODE to item.videoCode)
            }
            viewHolder.binding.btnDelete.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                // #issue-158: 这里可能为空
                val item = getItem(position)
                item?.let {
                    val file = it.videoUri.toUri().toFile()
                    context.showAlertDialog {
                        setTitle(R.string.sure_to_delete)
                        setMessage(context.getString(R.string.prepare_to_delete_s, file.name))
                        setPositiveButton(R.string.confirm) { _, _ ->
                            if (file.exists()) file.delete()
                            fragment.viewModel.deleteDownloadHanimeBy(it.videoCode, it.quality)
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                }
            }
            viewHolder.binding.btnLocalPlayback.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener
                context.openDownloadedHanimeVideoLocally(item.videoUri, onFileNotFound = {
                    context.showAlertDialog {
                        setTitle(R.string.video_not_exist)
                        setMessage(R.string.video_deleted_sure_to_delete_item)
                        setPositiveButton(R.string.delete) { _, _ ->
                            fragment.viewModel.deleteDownloadHanimeBy(item.videoCode, item.quality)
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                })
            }
        }
    }
}