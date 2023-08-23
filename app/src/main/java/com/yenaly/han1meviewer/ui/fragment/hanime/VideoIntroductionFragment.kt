package com.yenaly.han1meviewer.ui.fragment.hanime

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import coil.load
import coil.transform.CircleCropTransformation
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
import com.yenaly.han1meviewer.util.setDrawableTop
import com.yenaly.han1meviewer.util.showAlertDialog
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

        binding.playList.rv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.playList.rv.adapter = playListAdapter
        binding.relatedHanime.rv.layoutManager = GridLayoutManager(context, VIDEO_IN_ONE_LINE)
        binding.relatedHanime.rv.adapter = relatedAdapter
    }

    @SuppressLint("SetTextI18n")
    override fun liveDataObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.hanimeVideoFlow.collect { state ->
                    binding.videoIntroNsv.isInvisible = state !is VideoLoadingState.Success
                    when (state) {
                        is VideoLoadingState.Error -> Unit

                        is VideoLoadingState.Loading -> Unit

                        is VideoLoadingState.Success -> {
                            videoData = state.info

                            binding.title.text = state.info.title.also { initShareButton(it) }
                            binding.uploadTime.text =
                                TimeUtil.date2String(state.info.uploadTime, DATE_FORMAT)
                            binding.views.text = "${state.info.views.toString()}次"
                            binding.tvIntroduction.setContent(state.info.introduction)
                            binding.tags.setTags(state.info.tags)
                            if (state.info.playList != null) {
                                binding.playList.subTitle.text = state.info.playList.playListName
                                playListAdapter.setList(state.info.playList.video)
                            } else {
                                binding.playList.root.isGone = true
                            }
                            initArtist(state.info.artist)
                            relatedAdapter.setList(state.info.relatedHanimes)
                            initDownloadButton(state.info)
                            initFunctionBar(state.info)
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
                            showShortToast("喜歡失敗")
                        }

                        is WebsiteState.Loading -> Unit
                        is WebsiteState.Success -> {
                            if (videoData.isFav) {
                                showShortToast("取消喜歡")
                                videoData.decrementFavTime()
                                handleFavButton(false)
                            } else {
                                showShortToast("添加喜歡")
                                videoData.incrementFavTime()
                                handleFavButton(true)
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.loadDownloadedFlow.collect { entity ->
                    if (entity == null) { // 没下
                        enqueueDownloadWork(videoData)
                        return@collect
                    }
                    // 没下完或下過了
                    if (!entity.isDownloaded) {
                        if (entity.isDownloading) showShortToast("正在下載中...")
                        else showShortToast("正在佇列中...")
                        return@collect
                    }
                }
            }
        }
    }

    private fun handleFavButton(isFav: Boolean) {
        if (isFav) {
            binding.btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_24)
            binding.btnAddToFav.text = "已喜歡"
        } else {
            binding.btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_border_24)
            binding.btnAddToFav.text = "加入喜歡"
        }
    }

    private fun initArtist(artist: HanimeVideoModel.Artist?) {
        if (artist == null) {
            binding.vgArtist.isGone = true
        } else {
            binding.vgArtist.isGone = false
            binding.tvArtist.text = artist.name
            binding.tvGenre.text = artist.genre
            binding.ivArtist.load(artist.avatarUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    private fun initFunctionBar(videoData: HanimeVideoModel) {
        handleFavButton(videoData.isFav)
        binding.btnAddToFav.clickTrigger(viewLifecycleOwner.lifecycle) {
            if (isAlreadyLogin) {
                if (videoData.isFav) {
                    viewModel.removeFromFavVideo(
                        viewModel.videoCode,
                        videoData.currentUserId,
                    )
                } else {
                    viewModel.addToFavVideo(
                        viewModel.videoCode,
                        videoData.currentUserId,
                    )
                }
            } else {
                showShortToast(R.string.login_first)
            }
        }
        binding.btnWatchLater.clickTrigger(viewLifecycleOwner.lifecycle) {
            // todo
            showShortToast("功能暫未實現！")
        }
        binding.btnToWebpage.clickTrigger(viewLifecycleOwner.lifecycle) {
            browse(getHanimeVideoLink(viewModel.videoCode))
        }
    }

    private fun initShareButton(title: String) {
        val shareText =
            title + "\n" + getHanimeVideoLink(viewModel.videoCode) + "\n" + "- From Han1meViewer -"
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
        if (videoData.videoUrls.isEmpty()) {
            showShortToast(R.string.no_video_links_found)
        } else binding.btnDownload.clickTrigger(viewLifecycleOwner.lifecycle) {
            XPopup.Builder(context)
                .atView(it)
                .asAttachList(videoData.videoUrls.keys.toTypedArray(), null) { _, key ->
                    if (key == HanimeResolution.RES_UNKNOWN) {
                        showShortToast("該影片特殊，無法下載")
                    } else notifyDownload(videoData.title, key) {
                        checkedQuality = key
                        viewModel.findDownloadedHanimeByVideoCodeAndQuality(
                            viewModel.videoCode, checkedQuality
                        )
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
        requireContext().showAlertDialog {
            setTitle("確定要下載嗎？")
            setMessage(notifyMsg)
            setPositiveButton("是的") { _, _ ->
                action.invoke()
            }
            setNegativeButton("算了吧", null)
        }
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

    private fun enqueueDownloadWork(videoData: HanimeVideoModel) {
        requestNotificationPermission {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                HanimeDownloadWorker.QUALITY to checkedQuality,
                HanimeDownloadWorker.DOWNLOAD_URL to videoData.videoUrls[checkedQuality]!!,
                HanimeDownloadWorker.HANIME_NAME to videoData.title,
                HanimeDownloadWorker.VIDEO_CODE to viewModel.videoCode,
                HanimeDownloadWorker.COVER_URL to videoData.coverUrl,
            )
            val downloadRequest = OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
                .addTag(HanimeDownloadWorker.TAG)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
            WorkManager.getInstance(applicationContext)
                .beginUniqueWork(viewModel.videoCode, ExistingWorkPolicy.REPLACE, downloadRequest)
                .enqueue()
        }
    }
}