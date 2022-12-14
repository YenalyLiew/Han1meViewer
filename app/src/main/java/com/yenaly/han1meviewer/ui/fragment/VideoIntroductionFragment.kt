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
import com.permissionx.guolindev.PermissionX
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.logic.model.HanimeVideoModel
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.service.HanimeDownloadWorker
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.checkDownloadedHanimeFile
import com.yenaly.han1meviewer.util.createTags
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
                            showShortToast("????????????")
                        }
                        is WebsiteState.Loading -> Unit
                        is WebsiteState.Success -> {
                            showShortToast("????????????")
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
                    if (entity == null) { // ??????????????????
                        // ????????????????????????????????????????????????????????????????????????????????????????????????
                        if (checkDownloadedHanimeFile(videoData.title)) {
                            showShortToast("???????????????????????????")
                        } else {
                            enqueueDownloadWork(
                                videoData.title, checkedQuality,
                                videoData.videoUrls[checkedQuality]!!,
                                viewModel.videoCode, videoData.coverUrl,
                                releaseDate
                            )
                        }
                    } else { // ?????????
                        if (entity.quality == checkedQuality) {
                            showShortToast("??????????????????")
                        } else {
                            val msg = spannable {
                                "????????????????????????".text()
                                newline()
                                entity.title.span {
                                    style(Typeface.BOLD)
                                }
                                newline()
                                "????????????????????????".text()
                                newline()
                                entity.quality.span {
                                    style(Typeface.BOLD)
                                }
                                newline(2)
                                "????????????????????????????????????".text()
                            }
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("?????????????????????????????????????????????")
                                .setMessage(msg)
                                .setPositiveButton("??????") { _, _ ->
                                    enqueueDownloadWork(
                                        videoData.title, checkedQuality,
                                        videoData.videoUrls[checkedQuality]!!,
                                        viewModel.videoCode, videoData.coverUrl,
                                        releaseDate
                                    )
                                }.setNegativeButton("????????????", null).show()
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
            showShortToast("?????????????????????")
        }
    }

    private fun initShareButton(title: String) {
        val shareText =
            title + "\n" + getHanimeVideoLink(viewModel.videoCode) + "\n" + "- From Han1meViewer -"
        binding.btnShare.setOnClickListener {
            shareText(shareText, "???????????????????????????????????????")
        }
        binding.btnShare.setOnLongClickListener {
            copyTextToClipboard(shareText)
            showShortToast(R.string.copy_to_clipboard)
            return@setOnLongClickListener true
        }
    }

    private fun initDownloadButton(videoData: HanimeVideoModel) {
        if (videoData.videoUrls.isEmpty()) {
            showShortToast(R.string.no_video_links_found)
        } else binding.btnDownload.clickTrigger(viewLifecycleOwner.lifecycle) {
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
            "?????????????????????????????????".text()
            newline(2)
            "?????????".text()
            newline()
            title.span {
                style(Typeface.BOLD)
            }
            newline()
            "?????????".text()
            newline()
            quality.span {
                style(Typeface.BOLD)
            }
            newline(2)
            "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????".text()
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("?????????????????????")
            .setMessage(notifyMsg)
            .setPositiveButton("??????") { _, _ ->
                action.invoke()
            }
            .setNegativeButton("?????????", null)
            .show()
    }

    private inline fun requestNotificationPermission(crossinline then: () -> Unit) {
        PermissionX.init(this).permissions(PermissionX.permission.POST_NOTIFICATIONS)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.reason_for_download_notification),
                    getString(R.string.allow), getString(R.string.deny)
                )
            }.request { allGranted, _, _ ->
                if (!allGranted) showShortToast(R.string.msg_deny_download_notification)
                then.invoke()
            }
    }

    private fun enqueueDownloadWork(
        title: String, quality: String, relatedUrl: String,
        videoCode: String, coverUrl: String, releaseDate: Long
    ) {
        requestNotificationPermission {
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
}