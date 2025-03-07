package com.yenaly.han1meviewer.ui.adapter

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.google.android.material.button.MaterialButton
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemHanimeDownloadingBinding
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.state.DownloadState
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.util.HImageMeower.loadUnhappily
import com.yenaly.han1meviewer.util.addUpdateListener
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadManagerV2
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.utils.dpF
import com.yenaly.yenaly_libs.utils.formatFileSizeV2
import com.yenaly.yenaly_libs.utils.logFieldsChange
import java.lang.ref.WeakReference

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

        private val interpolator = FastOutSlowInInterpolator()

        private const val DOWNLOADING = 1
        private const val STATE = 1 shl 1
        private const val PROGRESS = 1 shl 2

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
                logFieldsChange(TAG, oldItem, newItem)
                var bitset = 0
                if (oldItem.downloadedLength != newItem.downloadedLength)
                    bitset = bitset or DOWNLOADING
                if (oldItem.state != newItem.state)
                    bitset = bitset or STATE
                if (oldItem.progress != newItem.progress)
                    bitset = bitset or PROGRESS
                return bitset
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val renderEffect = RenderEffect.createBlurEffect(
        8.dpF, 8.dpF,
        Shader.TileMode.CLAMP
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemHanimeDownloadingBinding>,
        position: Int,
        item: HanimeDownloadEntity?,
    ) {
        item ?: return
        holder.binding.tvTitle.text = item.title
        holder.binding.ivCover.loadUnhappily(item.coverUri, item.coverUrl)
        Log.d(TAG, "调用 load")
        holder.itemView.post {
            holder.binding.vCoverBg.updateLayoutParams {
                height = holder.itemView.height
            }
            holder.binding.ivCoverBg.updateLayoutParams {
                height = holder.itemView.height
            }
        }
        holder.binding.clProgress.post {
            holder.binding.vProgress.updateLayoutParams {
                width = holder.binding.clProgress.width * item.progress / 100
            }
        }
        holder.binding.ivCoverBg.apply {
            loadUnhappily(item.coverUri, item.coverUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRenderEffect(renderEffect)
            }
        }
        holder.binding.tvDownloadedSize.text = item.downloadedLength.formatFileSizeV2()
        holder.binding.tvSize.text = item.length.formatFileSizeV2()
        holder.binding.tvQuality.text = item.quality
//        holder.binding.tvProgress.text = "${item.progress}%"
//        holder.binding.pbProgress.setProgress(item.progress, true)
        holder.binding.btnStart.handleStartButton(item)
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
            holder.binding.btnStart.handleStartButton(item)
            holder.binding.tvDownloadedSize.text = item.downloadedLength.formatFileSizeV2()
        }
        if (bitset and STATE != 0) {
            holder.binding.btnStart.handleStartButton(item)
        }
        if (bitset and PROGRESS != 0) {
//            holder.binding.tvProgress.text = "${item.progress}%"
//            holder.binding.pbProgress.setProgress(item.progress, true)

            // 根据百分比，设置 vProgress 的宽度，比如 50% 就设置成 itemView 50% 的宽度
            val weakViewProgress = WeakReference(holder.binding.vProgress)
            ValueAnimator.ofInt(
                holder.binding.vProgress.width,
                holder.itemView.width * item.progress / 100
            ).apply {
                // must be less than HanimeDownloadWorker.RESPONSE_INTERVAL
                duration = 300L.coerceAtMost(HanimeDownloadWorker.RESPONSE_INTERVAL)
                interpolator = HanimeDownloadingRvAdapter.interpolator
                addUpdateListener(fragment) {
                    weakViewProgress.get()?.updateLayoutParams {
                        width = it.animatedValue as Int
                    }
                }
            }.start()
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
                viewHolder.binding.btnStart.handleStartButton(item, rotate = true)
            }
            viewHolder.binding.btnCancel.setOnClickListener {
                val pos = viewHolder.bindingAdapterPosition
                val item = getItem(pos) ?: return@setOnClickListener
                context.showAlertDialog {
                    setTitle(R.string.sure_to_delete)
                    setMessage(
                        context.getString(R.string.prepare_to_delete_s, item.title)
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


    @SuppressLint("SetTextI18n")
    private fun MaterialButton.handleStartButton(
        item: HanimeDownloadEntity,
        rotate: Boolean = false
    ) {
        val state = if (rotate) {
            when (item.state) {
                DownloadState.Unknown,
                DownloadState.Queued,
                DownloadState.Paused -> DownloadState.Downloading

                DownloadState.Downloading -> DownloadState.Paused

                DownloadState.Finished,
                DownloadState.Failed -> DownloadState.Unknown
            }
        } else {
            item.state
        }
        when (state) {
            DownloadState.Queued -> {
                setText(R.string.already_in_queue)
                setIconResource(R.drawable.ic_baseline_play_arrow_24)
            }

            DownloadState.Downloading -> {
//                setText(R.string.pause)
//                setIconResource(R.drawable.ic_baseline_pause_24)
                text = "${item.progress}%"
                setIconResource(R.drawable.ic_baseline_pause_24)
            }

            DownloadState.Paused -> {
                setText(R.string.continues)
                setIconResource(R.drawable.ic_baseline_play_arrow_24)
            }

            DownloadState.Failed -> {
                setText(R.string.retry)
                setIconResource(R.drawable.baseline_error_outline_24)
            }

            DownloadState.Finished, DownloadState.Unknown -> {} // do nothing
        }
    }
}