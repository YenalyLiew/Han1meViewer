package com.yenaly.han1meviewer.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.google.android.material.button.MaterialButton
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadingBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.util.createDownloadName
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.formatFileSize

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/19 019 21:11
 */
class HanimeDownloadingRvAdapter(private val fragment: DownloadingFragment) :
    BaseQuickAdapter<HanimeDownloadEntity, HanimeDownloadingRvAdapter.ViewHolder>(R.layout.item_hanime_downloading) {

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

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemHanimeDownloadingBinding>(view) {
        val binding = dataBinding!!
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: ViewHolder, item: HanimeDownloadEntity) {
        holder.binding.tvTitle.text = item.title
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvSize.text = spannable {
            item.downloadedLength.formatFileSize().text()
            " | ".span {
                color(Color.RED)
            }
            item.length.formatFileSize().span { style(Typeface.BOLD) }
        }
        holder.binding.tvQuality.text = item.quality
        holder.binding.tvProgress.text = "${item.progress}%"
        holder.binding.pbProgress.setProgress(item.progress, false)
        holder.binding.btnStart.handleStartButton(item.isDownloading)
    }

    private fun MaterialButton.handleStartButton(isDownloading: Boolean) {
        if (isDownloading) {
            setText(R.string.pause)
            setIconResource(R.drawable.ic_baseline_pause_24)
        } else {
            setText(R.string.continues)
            setIconResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.binding.btnStart.setOnClickListener {
            val pos = viewHolder.bindingAdapterPosition
            val item = getItem(pos)
            if (item.isDownloading) {
                item.isDownloading = false
                WorkManager.getInstance(context.applicationContext)
                    .cancelUniqueWorkAndPause(item)
            } else {
                item.isDownloading = true
                continueWork(item)
            }
            viewHolder.binding.btnStart.handleStartButton(item.isDownloading)
        }
        viewHolder.binding.btnCancel.setOnClickListener {
            val pos = viewHolder.bindingAdapterPosition
            val item = getItem(pos)
            context.showAlertDialog {
                setTitle("你確定要刪除嗎？")
                setMessage(
                    "你現在正要準備刪除" + "\n" + createDownloadName(
                        item.title, item.quality
                    )
                )
                setPositiveButton("沒錯") { _, _ ->
                    WorkManager.getInstance(context.applicationContext)
                        .cancelUniqueWorkAndDelete(item)
                }
                setNegativeButton("算了", null)
            }
        }
    }

    private fun continueWork(entity: HanimeDownloadEntity) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val data = workDataOf(
            HanimeDownloadWorker.QUALITY to entity.quality,
            HanimeDownloadWorker.DOWNLOAD_URL to entity.videoUrl,
            HanimeDownloadWorker.HANIME_NAME to entity.title,
            HanimeDownloadWorker.VIDEO_CODE to entity.videoCode,
            HanimeDownloadWorker.COVER_URL to entity.coverUrl,
        )
        val downloadRequest = OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
            .addTag(HanimeDownloadWorker.TAG)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .beginUniqueWork(entity.videoCode, ExistingWorkPolicy.REPLACE, downloadRequest)
            .enqueue()
    }

    private fun WorkManager.cancelUniqueWorkAndDelete(
        entity: HanimeDownloadEntity,
        workName: String = entity.videoCode,
    ) {
        cancelUniqueWork(workName)
        val file = entity.videoUri.toUri().toFile()
        if (file.exists()) file.delete()
        fragment.viewModel.deleteDownloadHanimeBy(entity.videoCode, entity.quality)
    }

    private fun WorkManager.cancelUniqueWorkAndPause(
        entity: HanimeDownloadEntity,
        workName: String = entity.videoCode,
    ) {
        cancelUniqueWork(workName)
        fragment.viewModel.updateDownloadHanime(entity)
    }
}