package com.yenaly.han1meviewer.ui.fragment.video

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.load
import coil.transform.CircleCropTransformation
import com.itxca.spannablex.spannable
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.ADVANCED_SEARCH_MAP
import com.yenaly.han1meviewer.HAdvancedSearch
import com.yenaly.han1meviewer.HanimeResolution
import com.yenaly.han1meviewer.LOCAL_DATE_FORMAT
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.SIMPLIFIED_VIDEO_IN_ONE_LINE
import com.yenaly.han1meviewer.UsingCautiously
import com.yenaly.han1meviewer.VIDEO_IN_ONE_LINE
import com.yenaly.han1meviewer.VIDEO_LAYOUT_MATCH_PARENT
import com.yenaly.han1meviewer.VIDEO_LAYOUT_WRAP_CONTENT
import com.yenaly.han1meviewer.advancedSearchMapOf
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.getHanimeVideoDownloadLink
import com.yenaly.han1meviewer.getHanimeVideoLink
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.HanimeVideo
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.adapter.FixedGridLayoutManager
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.requestPostNotificationPermission
import com.yenaly.han1meviewer.util.setDrawableTop
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.shareText
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import kotlinx.coroutines.launch
import kotlinx.datetime.format

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class VideoIntroductionFragment :
    YenalyFragment<FragmentVideoIntroductionBinding, VideoViewModel>() {

    @UsingCautiously("use after [viewModel.hanimeVideoFlow.collect]")
    private lateinit var videoData: HanimeVideo

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

        // #issue-80: 长按复制功能请求
        binding.title.setOnLongClickListener {
            binding.title.text.copyToClipboard()
            showShortToast(R.string.copy_to_clipboard)
            return@setOnLongClickListener true
        }
        binding.chineseTitle.setOnLongClickListener {
            binding.chineseTitle.text.copyToClipboard()
            showShortToast(R.string.copy_to_clipboard)
            return@setOnLongClickListener true
        }

        binding.relatedHanime.rv.layoutManager =
            FixedGridLayoutManager(context, VIDEO_IN_ONE_LINE)
        binding.playlist.rv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.playlist.rv.adapter = playlistAdapter
        binding.relatedHanime.rv.adapter = relatedAdapter
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.hanimeVideoFlow.collect { state ->
                    binding.videoIntroNsv.isInvisible = state !is VideoLoadingState.Success
                    when (state) {
                        is VideoLoadingState.Error -> Unit

                        is VideoLoadingState.Loading -> Unit

                        is VideoLoadingState.Success -> {
                            videoData = state.info

                            initTitle(state.info)
                            binding.uploadTime.text = state.info.uploadTime
                                ?.format(LOCAL_DATE_FORMAT)
                            binding.views.text =
                                getString(R.string.s_view_times, state.info.views.toString())
                            binding.tvIntroduction.setContent(state.info.introduction)
                            binding.tags.tags = state.info.tags
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
            viewModel.addToFavVideoFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.fav_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        if (videoData.isFav) {
                            showShortToast(R.string.cancel_fav)
                            videoData.decrementFavTime()
                            handleFavButton(false)
                        } else {
                            showShortToast(R.string.add_to_fav)
                            videoData.incrementFavTime()
                            handleFavButton(true)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDownloadedFlow.collect { entity ->
                if (entity == null) { // 没下
                    enqueueDownloadWork(videoData)
                    return@collect
                }
                // 没下完或下過了
                if (!entity.isDownloaded) {
                    if (entity.isDownloading) showShortToast(R.string.downloading_now)
                    else showShortToast(R.string.already_in_queue)
                    return@collect
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.modifyMyListFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.add_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        val index = state.info
                        checkNotNull(videoData.myList?.myListInfo?.get(index)).let {
                            it.isSelected = !it.isSelected
                        }
                        showShortToast(R.string.add_success)
                    }
                }
            }
        }
    }

    private fun initTitle(info: HanimeVideo) {
        binding.title.text = info.title.also { initShareButton(it) }
        binding.chineseTitle.text = info.chineseTitle
    }

    private fun handleFavButton(isFav: Boolean) {
        if (isFav) {
            binding.btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_24)
            binding.btnAddToFav.setText(R.string.liked)
        } else {
            binding.btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_border_24)
            binding.btnAddToFav.setText(R.string.add_to_fav)
        }
    }

    private fun initArtist(artist: HanimeVideo.Artist?) {
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

    private fun initFunctionBar(videoData: HanimeVideo) {
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
            shareText(shareText, getString(R.string.long_press_share_to_copy))
        }
        binding.btnShare.setOnLongClickListener {
            shareText.copyToClipboard()
            showShortToast(R.string.copy_to_clipboard)
            return@setOnLongClickListener true
        }
    }

    private fun initDownloadButton(videoData: HanimeVideo) {
        if (videoData.videoUrls.isEmpty()) {
            showShortToast(R.string.no_video_links_found)
        } else binding.btnDownload.clickTrigger(viewLifecycleOwner.lifecycle) {
            XPopup.Builder(context)
                .atView(it)
                .asAttachList(videoData.videoUrls.keys.toTypedArray(), null) { _, key ->
                    if (key == HanimeResolution.RES_UNKNOWN) {
                        showShortToast(R.string.cannot_download_here)
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
            getString(R.string.download_video_detail_below).text()
            newline(2)
            getString(R.string.name_with_colon).text()
            newline()
            title.span {
                style(Typeface.BOLD)
            }
            newline()
            getString(R.string.quality_with_colon).text()
            newline()
            quality.span {
                style(Typeface.BOLD)
            }
            newline(2)
            getString(R.string.after_download_tips).text()
        }
        requireContext().showAlertDialog {
            setTitle(R.string.sure_to_download)
            setMessage(notifyMsg)
            setPositiveButton(R.string.sure) { _, _ ->
                action.invoke()
            }
            setNegativeButton(R.string.no, null)
            setNeutralButton(R.string.go_to_official) { _, _ ->
                browse(getHanimeVideoDownloadLink(viewModel.videoCode))
            }
        }
    }

    private suspend fun enqueueDownloadWork(videoData: HanimeVideo) {
        requireContext().requestPostNotificationPermission()
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

    private fun List<HanimeInfo>.buildFlexibleGridLayoutManager(): GridLayoutManager {
        val counts = if (any { it.itemType == HanimeInfo.NORMAL })
            VIDEO_IN_ONE_LINE else SIMPLIFIED_VIDEO_IN_ONE_LINE
        return FixedGridLayoutManager(context, counts)
    }
}