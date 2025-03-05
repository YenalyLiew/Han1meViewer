package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.HFileManager
import com.yenaly.han1meviewer.LOCAL_DATE_TIME_FORMAT
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadedBinding
import com.yenaly.han1meviewer.logic.entity.download.VideoWithCategories
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.han1meviewer.util.HImageMeower.loadUnhappily
import com.yenaly.han1meviewer.util.openDownloadedHanimeVideoInActivity
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.formatFileSizeV2
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
    BaseDifferAdapter<VideoWithCategories, DataBindingHolder<ItemHanimeDownloadedBinding>>(
        COMPARATOR
    ) {

    init {
        isStateViewEnable = true
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<VideoWithCategories>() {
            override fun areContentsTheSame(
                oldItem: VideoWithCategories,
                newItem: VideoWithCategories,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: VideoWithCategories,
                newItem: VideoWithCategories,
            ): Boolean {
                return oldItem.video.id == newItem.video.id
            }
        }
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemHanimeDownloadedBinding>,
        position: Int,
        item: VideoWithCategories?,
    ) {
        item ?: return
        holder.binding.tvTitle.text = item.video.title
        holder.binding.ivCover.loadUnhappily(item.video.coverUrl, item.video.coverUri)
        holder.binding.tvAddedTime.text =
            Instant.fromEpochMilliseconds(item.video.addDate).toLocalDateTime(
                TimeZone.currentSystemDefault()
            ).format(LOCAL_DATE_TIME_FORMAT)
        holder.binding.tvQuality.text = spannable {
            item.video.quality.text()
            " | ".span {
                color(Color.RED)
            }
            item.video.videoUri.toUri().toFile().length().formatFileSizeV2().text()
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
                context.activity?.startActivity<VideoActivity>(VIDEO_CODE to item.video.videoCode)
            }
            viewHolder.binding.btnDelete.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                // #issue-158: 这里可能为空
                val item = getItem(position)
                item?.let {
                    val file = it.video.videoUri.toUri().toFile()
                    context.showAlertDialog {
                        setTitle(R.string.sure_to_delete)
                        setMessage(context.getString(R.string.prepare_to_delete_s, file.name))
                        setPositiveButton(R.string.confirm) { _, _ ->
                            // if (file.exists()) file.delete()
                            HFileManager.getDownloadVideoFolder(
                                it.video.videoCode
                            ).deleteRecursively()
                            fragment.viewModel.deleteDownloadHanimeBy(
                                it.video.videoCode,
                                it.video.quality
                            )
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                }
            }
            viewHolder.binding.btnLocalPlayback.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener
//                context.openDownloadedHanimeVideoLocally(item.video.videoUri, onFileNotFound = {
//                    context.showAlertDialog {
//                        setTitle(R.string.video_not_exist)
//                        setMessage(R.string.video_deleted_sure_to_delete_item)
//                        setPositiveButton(R.string.delete) { _, _ ->
//                            fragment.viewModel.deleteDownloadHanimeBy(
//                                item.video.videoCode,
//                                item.video.quality
//                            )
//                        }
//                        setNegativeButton(R.string.cancel, null)
//                    }
//                })
                context.openDownloadedHanimeVideoInActivity(item.video.videoCode)
            }
        }
    }
}