package com.yenaly.han1meviewer.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.google.android.material.button.MaterialButton
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.HFileManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadingBinding
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.util.HImageMeower.loadUnhappily
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadManagerV2
import com.yenaly.yenaly_libs.utils.formatFileSizeV2

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/26 026 17:05
 */
class HanimeDownloadingRvAdapter(private val fragment: DownloadingFragment) :
    BaseDifferAdapter<HanimeDownloadEntity, DataBindingHolder<ItemHanimeDownloadingBinding>>(
        COMPARATOR
    ) {

    init {
        isStateViewEnable = true
    }

    companion object {
        const val TAG = "HanimeDownloadingRvAdapter"

        private const val DOWNLOADING = 1
        private const val PAUSE = 1 shl 1

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

            override fun getChangePayload(
                oldItem: HanimeDownloadEntity,
                newItem: HanimeDownloadEntity,
            ): Any {
                var bitset = 0
                if (oldItem.progress != newItem.progress || oldItem.downloadedLength != newItem.downloadedLength)
                    bitset = bitset or DOWNLOADING
                if (oldItem.isDownloading != newItem.isDownloading)
                    bitset = bitset or PAUSE
                if (oldItem.state != newItem.state) {
                    Log.d(TAG, "${oldItem.videoCode}: ${oldItem.state} -> ${newItem.state}")
                }
                return bitset
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemHanimeDownloadingBinding>,
        position: Int,
        item: HanimeDownloadEntity?,
    ) {
        item ?: return
        holder.binding.tvTitle.text = item.title
        holder.binding.ivCover.loadUnhappily(item.coverUrl, item.coverUri)
        holder.binding.tvSize.text = spannable {
            item.downloadedLength.formatFileSizeV2().text()
            " | ".span { color(Color.RED) }
            item.length.formatFileSizeV2().span { style(Typeface.BOLD) }
        }
        holder.binding.tvQuality.text = item.quality
        holder.binding.tvProgress.text = "${item.progress}%"
        holder.binding.pbProgress.setProgress(item.progress, true)
        holder.binding.btnStart.handleStartButton(item.isDownloading)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemHanimeDownloadingBinding>,
        position: Int,
        item: HanimeDownloadEntity?,
        payloads: List<Any>,
    ) {
        if (payloads.isEmpty() || payloads.first() == 0)
            return super.onBindViewHolder(holder, position, item, payloads)
        item ?: return
        val bitset = payloads.first() as Int
        if (bitset and DOWNLOADING != 0) {
            holder.binding.tvSize.text = spannable {
                item.downloadedLength.formatFileSizeV2().text()
                " | ".span { color(Color.RED) }
                item.length.formatFileSizeV2().span { style(Typeface.BOLD) }
            }
            holder.binding.tvProgress.text = "${item.progress}%"
            holder.binding.pbProgress.setProgress(item.progress, true)
        }
        if (bitset and PAUSE != 0) {
            holder.binding.btnStart.handleStartButton(item.isDownloading)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): DataBindingHolder<ItemHanimeDownloadingBinding> {
        return DataBindingHolder(
            ItemHanimeDownloadingBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        ).also { viewHolder ->
            viewHolder.binding.btnStart.setOnClickListener {
                val pos = viewHolder.bindingAdapterPosition
                val item = getItem(pos) ?: return@setOnClickListener
//                val isDownloading: Boolean
                if (item.isDownloading) {
//                    isDownloading = false
//                    cancelUniqueWorkAndPause(item.copy(isDownloading = false))
                    HanimeDownloadManagerV2.stopTask(item)
                } else {
//                    isDownloading = true
//                    continueWork(item.copy(isDownloading = true))
                    HanimeDownloadManagerV2.resumeTask(item)
                }
                viewHolder.binding.btnStart.handleStartButton(!item.isDownloading)
            }
            viewHolder.binding.btnCancel.setOnClickListener {
                val pos = viewHolder.bindingAdapterPosition
                val item = getItem(pos) ?: return@setOnClickListener
                context.showAlertDialog {
                    setTitle(R.string.sure_to_delete)
                    setMessage(
                        context.getString(
                            R.string.prepare_to_delete_s, item.videoUri.toUri().toFile().path
                        )
                    )
                    setPositiveButton(R.string.confirm) { _, _ ->
//                        cancelUniqueWorkAndDelete(item)
                        HanimeDownloadManagerV2.deleteTask(item)
                    }
                    setNegativeButton(R.string.cancel, null)
                }
            }
        }
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

    fun continueWork(entity: HanimeDownloadEntity) {
        // HanimeDownloadManager.resumeTask(entity)
        HanimeDownloadManagerV2.resumeTask(entity)
    }

    private fun cancelUniqueWorkAndDelete(
        entity: HanimeDownloadEntity
    ) {
        // cancelOrReplaceUniqueWork(entity)
        HanimeDownloadManagerV2.stopTask(entity)
        // val file = entity.videoUri.toUri().toFile()
        // if (file.exists()) file.delete()
        HFileManager.getDownloadVideoFolder(entity.videoCode).deleteRecursively()
        fragment.viewModel.deleteDownloadHanimeBy(entity.videoCode, entity.quality)
    }

    fun cancelUniqueWorkAndPause(
        entity: HanimeDownloadEntity
    ) {
        // cancelOrReplaceUniqueWork(entity)
        HanimeDownloadManagerV2.stopTask(entity)
        fragment.viewModel.updateDownloadHanime(entity)
    }

//    private suspend fun cancelOrReplaceUniqueWork(
//        entity: HanimeDownloadEntity
//    ) {
//        val op = HanimeDownloadManager.stopTask(entity)
//        runSuspendCatching {
//            op.await()
//        }.onFailure { t ->
//            t.printStackTrace()
//            HanimeDownloadManager.deleteTaskCrazily(entity)
//        }
//    }
}