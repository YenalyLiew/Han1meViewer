package com.yenaly.han1meviewer.ui.fragment.video

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
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.logic.model.HanimeInfoModel
import com.yenaly.han1meviewer.logic.model.HanimeVideoModel
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.adapter.FixedGridLayoutManager
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.setDrawableTop
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
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

    private val playlistAdapter by unsafeLazy { HanimeVideoRvAdapter(VIDEO_LAYOUT_WRAP_CONTENT) }
    private val relatedAdapter by unsafeLazy { HanimeVideoRvAdapter(VIDEO_LAYOUT_MATCH_PARENT) }

    override fun initData(savedInstanceState: Bundle?) {
        binding.relatedHanime.subTitle.isGone = true
        binding.playlist.rv.isNestedScrollingEnabled = true
        binding.playlist.title.setText(R.string.series_video)
        binding.relatedHanime.title.setText(R.string.related_video)
        binding.tags.lifecycle = viewLifecycleOwner.lifecycle

        binding.relatedHanime.rv.layoutManager =
            FixedGridLayoutManager(context, VIDEO_IN_ONE_LINE)
        binding.playlist.rv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.playlist.rv.adapter = playlistAdapter
        binding.relatedHanime.rv.adapter = relatedAdapter
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.hanimeVideoFlow.collect { state ->
                    binding.videoIntroNsv.isInvisible = state !is VideoLoadingState.Success
                    when (state) {
                        is VideoLoadingState.Error -> Unit

                        is VideoLoadingState.Loading -> Unit

                        is VideoLoadingState.Success -> {
                            videoData = state.info

                            initTitle(state.info)
                            binding.uploadTime.text =
                                TimeUtil.date2String(state.info.uploadTime, DATE_FORMAT)
                            binding.views.text = "${state.info.views.toString()}次"
                            binding.tvIntroduction.setContent(state.info.introduction)
                            binding.tags.setTags(state.info.tags)
                            if (state.info.playlist != null) {
                                binding.playlist.subTitle.text = state.info.playlist.playlistName
                                playlistAdapter.submitList(state.info.playlist.video)
                            } else {
                                binding.playlist.root.isGone = true
                            }
                            binding.relatedHanime.rv.layoutManager =
                                state.info.relatedHanimes.buildFlexibleGridLayoutManager()
                            relatedAdapter.submitList(state.info.relatedHanimes)
                            initArtist(state.info.artist)
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
                        else showShortToast("已經在佇列中...")
                        return@collect
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.modifyMyListFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            showShortToast("操作失敗")
                        }

                        is WebsiteState.Loading -> Unit
                        is WebsiteState.Success -> {
                            val index = state.info
                            checkNotNull(videoData.myList?.myListInfo?.get(index)).let {
                                it.isSelected = !it.isSelected
                            }
                            showShortToast("操作成功")
                        }
                    }
                }
            }
        }
    }

    private fun initTitle(info: HanimeVideoModel) {
        binding.title.text = info.title.also { initShareButton(it) }
        binding.chineseTitle.text = info.chineseTitle
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
            binding.vgArtist.setOnClickListener {
                startActivity<SearchActivity>(
                    ADVANCED_SEARCH_MAP to advancedSearchMapOf(
                        HAdvancedSearch.QUERY to artist.name,
                        HAdvancedSearch.GENRE to artist.genre
                    )
                )
            }
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
        binding.btnMyList.setOnClickListener {
            if (isAlreadyLogin) {
                requireContext().showAlertDialog {
                    setTitle(R.string.add_to_playlist)
                    setMultiChoiceItems(
                        videoData.myList?.titleArray,
                        videoData.myList?.isSelectedArray,
                    ) { _, index, isChecked ->
                        viewModel.modifyMyList(
                            checkNotNull(videoData.myList?.myListInfo?.get(index)).code,
                            viewModel.videoCode, isChecked, index
                        )
                    }
                    setNeutralButton(R.string.back, null)
                }
            } else {
                showShortToast(R.string.login_first)
            }
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
                        showShortToast("暫時無法從這裏下載！")
                        browse(getHanimeVideoDownloadLink(viewModel.videoCode))
                    } else notifyDownload(videoData.title, key) {
                        checkedQuality = key
                        viewModel.findDownloadedHanime(
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
            "下載完畢後可以在「下載」介面找到下載後的影片，「設定」介面裏有詳細儲存路徑。".text()
        }
        requireContext().showAlertDialog {
            setTitle("確定要下載嗎？")
            setMessage(notifyMsg)
            setPositiveButton("是的") { _, _ ->
                action.invoke()
            }
            setNegativeButton("算了吧", null)
            setNeutralButton("轉到官方") { _, _ ->
                browse(getHanimeVideoDownloadLink(viewModel.videoCode))
            }
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

    private fun List<HanimeInfoModel>.buildFlexibleGridLayoutManager(): GridLayoutManager {
        val counts = if (any { it.itemType == HanimeInfoModel.NORMAL })
            VIDEO_IN_ONE_LINE else SIMPLIFIED_VIDEO_IN_ONE_LINE
        return FixedGridLayoutManager(context, counts)
    }
}