package com.yenaly.han1meviewer.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itxca.spannablex.spannable
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.logic.model.HanimeVideoModel
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.service.HanimeDownloadWorker
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.*
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class VideoIntroductionFragment :
    YenalyFragment<FragmentVideoIntroductionBinding, VideoViewModel>() {

    @UsingCautiously("use after [viewModel.hanimeVideoFlow.collect]")
    private lateinit var videoData: HanimeVideoModel

    @UsingCautiously("use after [Xpopup#asAttachList]")
    private lateinit var checkedQuality: String

    private val playListAdapter by unsafeLazy { HanimeVideoRvAdapter(VIDEO_LAYOUT_WRAP_CONTENT) }
    private val relatedAdapter by unsafeLazy { HanimeVideoRvAdapter(VIDEO_LAYOUT_MATCH_PARENT) }

    override fun initData(savedInstanceState: Bundle?) {
        binding.relatedHanime.subTitle.isGone = true
        binding.playList.rv.isNestedScrollingEnabled = true
        binding.playList.title.setText(R.string.series_video)
        binding.relatedHanime.title.setText(R.string.related_video)

        initFunctionBar()

        binding.playList.rv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.playList.rv.adapter = playListAdapter
        binding.relatedHanime.rv.layoutManager = GridLayoutManager(context, VIDEO_IN_ONE_LINE)
        binding.relatedHanime.rv.adapter = relatedAdapter
    }

    override fun liveDataObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.hanimeVideoFlow.collect { state ->
                    binding.videoIntroNsv.isInvisible = state !is VideoLoadingState.Success
                    when (state) {
                        is VideoLoadingState.Error -> {

                        }
                        is VideoLoadingState.Loading -> {

                        }
                        is VideoLoadingState.Success -> {

                            videoData = state.info

                            binding.title.text = state.info.title.also { initShareButton(it) }
                            binding.dateAndViews.text = state.info.uploadTimeWithViews
                            binding.tvIntroduction.setContent(state.info.introduction)
                            binding.tagGroup.createTags(state.info.tags)
                            if (state.info.playList != null) {
                                binding.playList.subTitle.text = state.info.playList.playListName
                                playListAdapter.setList(state.info.playList.video)
                            } else {
                                binding.playList.root.isGone = true
                            }
                            relatedAdapter.setList(state.info.relatedHanimes)
                            initDownloadButton(state.info)
                        }
                        is VideoLoadingState.NoContent -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.addToFavVideoFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast("喜愛失敗")
                        }
                        is WebsiteState.Loading -> Unit
                        is WebsiteState.Success -> {
                            showShortToast("喜愛成功")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.loadDownloadedFlow.collect { entity ->
                    val releaseDate = TimeUtil.string2Millis(
                        videoData.uploadTimeWithViews.substringBefore('|').trim(),
                        "yyyy-MM-dd"
                    )
                    if (entity == null) { // 没下完或没下
                        // 检测是否存在这个关于这个影片的文件，忽略分辨率，若有则不进行下载
                        if (checkDownloadedHanimeFile(videoData.title)) {
                            showShortToast("正在下載中，請稍等")
                        } else {
                            enqueueDownloadWork(
                                videoData.title, checkedQuality,
                                videoData.videoUrls[checkedQuality]!!,
                                viewModel.videoCode, videoData.coverUrl,
                                releaseDate
                            )
                        }
                    } else { // 下過了
                        if (entity.quality == checkedQuality) {
                            showShortToast("已經下載過咯")
                        } else {
                            val msg = spannable {
                                "存在的影片名稱：".text()
                                newline()
                                entity.title.span {
                                    style(Typeface.BOLD)
                                }
                                newline()
                                "存在的影片畫質：".text()
                                newline()
                                entity.quality.span {
                                    style(Typeface.BOLD)
                                }
                                newline(2)
                                "是否覆蓋原畫質進行下載？".text()
                            }
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("已經存在同名稱但非同畫質的影片")
                                .setMessage(msg)
                                .setPositiveButton("覆蓋") { _, _ ->
                                    enqueueDownloadWork(
                                        videoData.title, checkedQuality,
                                        videoData.videoUrls[checkedQuality]!!,
                                        viewModel.videoCode, videoData.coverUrl,
                                        releaseDate
                                    )
                                }.setNegativeButton("不下了！", null).show()
                        }
                    }
                }
            }
        }
    }

    private fun initFunctionBar() {
        binding.btnAddToFav.clickTrigger(viewLifecycleOwner.lifecycle) {
            if (isAlreadyLogin) {
                viewModel.addToFavVideo(
                    viewModel.videoCode,
                    videoData.currentUserId,
                    videoData.csrfToken
                )
            } else {
                showShortToast(R.string.login_first)
            }
        }
        binding.btnWatchLater.clickTrigger(viewLifecycleOwner.lifecycle) {
            // todo
            showShortToast("功能暫未實現！")
        }
    }

    private fun initShareButton(title: String) {
        val shareText = title + "\n" + getHanimeVideoLink(viewModel.videoCode) + "\n" + "- From Han1meViewer -"
        binding.btnShare.setOnClickListener {
            shareText(shareText, "長按分享可以複製到剪貼簿中")
        }
        binding.btnShare.setOnLongClickListener {
            copyTextToClipboard(shareText)
            showShortToast(R.string.copy_to_clipboard)
            return@setOnLongClickListener true
        }
    }

    private fun initDownloadButton(videoData: HanimeVideoModel) {
        binding.btnDownload.clickTrigger(viewLifecycleOwner.lifecycle) {
            XPopup.Builder(context)
                .atView(it)
                .asAttachList(videoData.videoUrls.keys.toTypedArray(), null) { _, key ->
                    notifyDownload(videoData.title, key) {
                        checkedQuality = key
                        viewModel.loadDownloadedHanimeByVideoCode(viewModel.videoCode)
                    }
                }.show()
        }
    }

    private fun notifyDownload(title: String, quality: String, action: () -> Unit) {
        val notifyMsg = spannable {
            "將要下載的影片詳情如下".text()
            newline(2)
            "名稱：".text()
            newline()
            title.span {
                style(Typeface.BOLD)
            }
            newline()
            "畫質：".text()
            newline()
            quality.span {
                style(Typeface.BOLD)
            }
            newline(2)
            "下載完畢後可以在「下載」介面找到下載後的影片，「設置」介面裏有詳細儲存路徑。".text()
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("確定要下載嗎？")
            .setMessage(notifyMsg)
            .setPositiveButton("是的") { _, _ ->
                action.invoke()
            }
            .setNegativeButton("算了吧", null)
            .show()
    }

    private fun enqueueDownloadWork(
        title: String, quality: String, relatedUrl: String,
        videoCode: String, coverUrl: String, releaseDate: Long
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val data = Data.Builder()
            .putString(HanimeDownloadWorker.QUALITY, quality)
            .putString(HanimeDownloadWorker.DOWNLOAD_URL, relatedUrl)
            .putString(HanimeDownloadWorker.HANIME_NAME, title)
            .putString(HanimeDownloadWorker.VIDEO_CODE, videoCode)
            .putString(HanimeDownloadWorker.COVER_URL, coverUrl)
            .putLong(HanimeDownloadWorker.RELEASE_DATE, releaseDate)
            .build()
        val downloadRequest = OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
            .addTag(HanimeDownloadWorker.TAG)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(videoCode, ExistingWorkPolicy.REPLACE, downloadRequest)
            .enqueue()
    }
}