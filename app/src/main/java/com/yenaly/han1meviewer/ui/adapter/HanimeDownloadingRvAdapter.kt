package com.yenaly.han1meviewer.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.load
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.google.android.material.button.MaterialButton
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadingBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.util.await
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.utils.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvSize.text = spannable {
            item.downloadedLength.formatFileSize().text()
            " | ".span { color(Color.RED) }
            item.length.formatFileSize().span { style(Typeface.BOLD) }
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
                item.downloadedLength.formatFileSize().text()
                " | ".span { color(Color.RED) }
                item.length.formatFileSize().span { style(Typeface.BOLD) }
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
                if (item.isDownloading) {
                    item.isDownloading = false
                    fragment.viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                        WorkManager.getInstance(context.applicationContext)
                            .cancelUniqueWorkAndPause(item)
                    }
                } else {
                    item.isDownloading = true
                    continueWork(item)
                }
                viewHolder.binding.btnStart.handleStartButton(item.isDownloading)
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
                        fragment.viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            WorkManager.getInstance(context.applicationContext)
                                .cancelUniqueWorkAndDelete(item)
                        }
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
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                HanimeDownloadWorker.BACKOFF_DELAY, TimeUnit.MILLISECONDS
            )
            .setInputData(data)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .beginUniqueWork(entity.videoCode, ExistingWorkPolicy.REPLACE, downloadRequest)
            .enqueue()
    }

    private suspend fun WorkManager.cancelUniqueWorkAndDelete(
        entity: HanimeDownloadEntity,
        workName: String = entity.videoCode,
    ) {
        cancelOrReplaceUniqueWork(entity, workName)
        val file = entity.videoUri.toUri().toFile()
        if (file.exists()) file.delete()
        fragment.viewModel.deleteDownloadHanimeBy(entity.videoCode, entity.quality)
    }

    suspend fun WorkManager.cancelUniqueWorkAndPause(
        entity: HanimeDownloadEntity,
        workName: String = entity.videoCode,
    ) {
        cancelOrReplaceUniqueWork(entity, workName)
        fragment.viewModel.updateDownloadHanime(entity)
    }

    private suspend fun WorkManager.cancelOrReplaceUniqueWork(
        entity: HanimeDownloadEntity,
        workName: String = entity.videoCode,
    ) {
        val op = cancelUniqueWork(workName)
        try {
            op.result.await()
        } catch (t: Throwable) {
            t.printStackTrace()
            // 必須另闢蹊徑，通過替換的方式來刪除，要不然無法真正地取消。
            val downloadRequest = OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
                .addTag(HanimeDownloadWorker.TAG)
                .setInputData(workDataOf(HanimeDownloadWorker.DELETE to true))
                .build()
            WorkManager.getInstance(context.applicationContext)
                .beginUniqueWork(workName, ExistingWorkPolicy.REPLACE, downloadRequest)
                .enqueue()
        }
    }
}