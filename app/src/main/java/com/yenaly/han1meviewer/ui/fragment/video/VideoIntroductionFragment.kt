package com.yenaly.han1meviewer.ui.fragment.video

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.itxca.spannablex.spannable
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.ADVANCED_SEARCH_MAP
import com.yenaly.han1meviewer.HAdvancedSearch
import com.yenaly.han1meviewer.HanimeResolution
import com.yenaly.han1meviewer.LOCAL_DATE_FORMAT
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_LAYOUT_MATCH_PARENT
import com.yenaly.han1meviewer.VIDEO_LAYOUT_WRAP_CONTENT
import com.yenaly.han1meviewer.VideoCoverSize
import com.yenaly.han1meviewer.advancedSearchMapOf
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.databinding.ItemVideoIntroductionBinding
import com.yenaly.han1meviewer.getHanimeVideoDownloadLink
import com.yenaly.han1meviewer.getHanimeVideoLink
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.HanimeVideo
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.adapter.AdapterLikeDataBindingPage
import com.yenaly.han1meviewer.ui.adapter.BaseSingleDifferAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.adapter.RvWrapper.Companion.wrappedWith
import com.yenaly.han1meviewer.ui.adapter.VideoColumnTitleAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.han1meviewer.util.requestPostNotificationPermission
import com.yenaly.han1meviewer.util.setDrawableTop
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.shareText
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import com.yenaly.yenaly_libs.utils.view.clickWithCondition
import com.yenaly.yenaly_libs.utils.view.findParent
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.format

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class VideoIntroductionFragment : YenalyFragment<FragmentVideoIntroductionBinding>() {

    companion object {
        private const val FAV = 1
        private const val PLAYLIST = 1 shl 1
        private const val SUBSCRIBE = 1 shl 2

        val COMPARATOR = object : DiffUtil.ItemCallback<HanimeVideo>() {
            override fun areItemsTheSame(oldItem: HanimeVideo, newItem: HanimeVideo): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: HanimeVideo, newItem: HanimeVideo): Boolean {
                return false
            }

            override fun getChangePayload(oldItem: HanimeVideo, newItem: HanimeVideo): Any {
                var bitset = 0
                if (oldItem.isFav != newItem.isFav)
                    bitset = bitset or FAV
                if (!(oldItem.myList?.isSelectedArray contentEquals newItem.myList?.isSelectedArray))
                    bitset = bitset or PLAYLIST
                if (oldItem.artist?.isSubscribed != newItem.artist?.isSubscribed)
                    bitset = bitset or SUBSCRIBE
                return bitset
            }
        }
    }

    val viewModel by activityViewModels<VideoViewModel>()

    private var checkedQuality: String? = null

    private val videoIntroAdapter = VideoIntroductionAdapter()
    private val playlistTitleAdapter =
        VideoColumnTitleAdapter(title = R.string.series_video, notifyWhenSet = true)
    private val playlistAdapter = HanimeVideoRvAdapter(VIDEO_LAYOUT_WRAP_CONTENT)
    private val playlistWrapper = playlistAdapter.wrappedWith {
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
    private val relatedTitleAdapter =
        VideoColumnTitleAdapter(title = R.string.related_video)
    private val relatedAdapter = HanimeVideoRvAdapter(VIDEO_LAYOUT_MATCH_PARENT)

    private val multi by unsafeLazy {
        // 后期添加
        ConcatAdapter()
    }

    private val layoutManager by unsafeLazy {
        GridLayoutManager(context, 1).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (multi.getWrappedAdapterAndPosition(position).first === relatedAdapter) {
                        return 1
                    }
                    return spanCount
                }
            }
        }
    }

    /**
     * 保证 submitList 不同时调用
     */
    private val mutex = Mutex()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentVideoIntroductionBinding {
        return FragmentVideoIntroductionBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        binding.rvVideoIntro.layoutManager = layoutManager
        binding.rvVideoIntro.adapter = multi
        binding.rvVideoIntro.addOnItemTouchListener(VideoIntroTouchListener())
        binding.rvVideoIntro.clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.rvVideoIntro) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = navBar.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.hanimeVideoStateFlow.collect { state ->
                    binding.rvVideoIntro.isVisible = state is VideoLoadingState.Success
                    when (state) {
                        is VideoLoadingState.Error -> Unit

                        is VideoLoadingState.Loading -> Unit

                        is VideoLoadingState.Success -> {
                            val video = state.info
                            mutex.withLock {
                                videoIntroAdapter.submit(video) // 挂起是为了让它在首位
                            }
                            multi.addAdapter(videoIntroAdapter)
                            if (video.playlist != null) {
                                playlistTitleAdapter.subtitle = video.playlist.playlistName
                                multi.addAdapter(playlistTitleAdapter)
                                playlistAdapter.submitList(video.playlist.video)
                                multi.addAdapter(playlistWrapper)
                            } else {
                                multi.removeAdapter(playlistTitleAdapter)
                                multi.removeAdapter(playlistWrapper)
                            }
                            multi.addAdapter(relatedTitleAdapter)
                            relatedAdapter.submitList(video.relatedHanimes)
                            multi.addAdapter(relatedAdapter)
                            layoutManager.spanCount = video.relatedHanimes.eachGridCounts
                        }

                        is VideoLoadingState.NoContent -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.hanimeVideoFlow.collect { video ->
                    mutex.withLock {
                        videoIntroAdapter.submit(video)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addToFavVideoFlow.collect { state ->
                videoIntroAdapter.binding?.btnAddToFav?.setTag(
                    R.id.click_condition, state != WebsiteState.Loading
                )
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.fav_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        val isFav = state.info
                        if (isFav) {
                            showShortToast(R.string.cancel_fav)
                        } else {
                            showShortToast(R.string.add_to_fav)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDownloadedFlow.collect { entity ->
                if (entity == null) { // 没下
                    viewModel.hanimeVideoFlow.value?.let {
                        val checkedQuality = requireNotNull(checkedQuality)
                        notifyDownload(it.title, checkedQuality) {
                            launch {
                                enqueueDownloadWork(it)
                            }
                        }
                    }
                    return@collect
                }
                if (entity.isDownloaded) {
                    // #issue-194: 重复下载提示&重新下载
                    viewModel.hanimeVideoFlow.value?.let {
                        val checkedQuality = requireNotNull(checkedQuality)
                        notifyDownload(it.title, checkedQuality, isRedownload = true) {
                            launch {
                                enqueueDownloadWork(it, isRedownload = true)
                            }
                        }
                    }
                } else {
                    // 没下完或下過了
                    if (entity.isDownloading) showShortToast(R.string.downloading_now)
                    else showShortToast(R.string.already_in_queue)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.modifyMyListFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.modify_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        showShortToast(R.string.modify_success)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subscribeArtistFlow.collect { state ->
                videoIntroAdapter.binding?.btnSubscribe?.setTag(
                    R.id.click_condition, state != WebsiteState.Loading
                )
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.subscribe_failed)
                    }

                    is WebsiteState.Loading -> Unit
                    is WebsiteState.Success -> {
                        if (state.info) {
                            showShortToast(R.string.subscribe_success)
                        } else {
                            showShortToast(R.string.unsubscribe_success)
                        }
                        activity?.setResult(Activity.RESULT_OK)
                    }
                }
            }
        }
    }

    private fun notifyDownload(
        title: String, quality: String, isRedownload: Boolean = false,
        action: () -> Unit
    ) {
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
            setTitle(if (isRedownload) R.string.sure_to_redownload else R.string.sure_to_download)
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

    private suspend fun enqueueDownloadWork(videoData: HanimeVideo, isRedownload: Boolean = false) {
        requireContext().requestPostNotificationPermission()
        val checkedQuality = requireNotNull(checkedQuality)
        val data = workDataOf(
            HanimeDownloadWorker.QUALITY to checkedQuality,
            HanimeDownloadWorker.DOWNLOAD_URL to videoData.videoUrls[checkedQuality]?.link,
            HanimeDownloadWorker.VIDEO_TYPE to videoData.videoUrls[checkedQuality]?.suffix,
            HanimeDownloadWorker.HANIME_NAME to videoData.title,
            HanimeDownloadWorker.VIDEO_CODE to viewModel.videoCode,
            HanimeDownloadWorker.COVER_URL to videoData.coverUrl,
            HanimeDownloadWorker.REDOWNLOAD to isRedownload,
        )
        val downloadRequest = HanimeDownloadWorker.build {
            setInputData(data)
        }
        WorkManager.getInstance(requireContext().applicationContext)
            .beginUniqueWork(viewModel.videoCode, ExistingWorkPolicy.REPLACE, downloadRequest)
            .enqueue()

    }

    private val List<HanimeInfo>.eachGridCounts
        get() = if (any { it.itemType == HanimeInfo.NORMAL }) {
            VideoCoverSize.Normal.videoInOneLine
        } else VideoCoverSize.Simplified.videoInOneLine

    private inner class VideoIntroTouchListener : OnItemTouchListener {

        private var startX = 0
        private var vp2: ViewPager2? = null
        private var isNotHorizontalWrapper = false

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = e.x.toInt()
                    val childView = rv.findChildViewUnder(e.x, e.y)
                    val position = childView?.let(rv::getChildAdapterPosition) ?: return false
                    val adapter = multi.getWrappedAdapterAndPosition(position).first
                    isNotHorizontalWrapper = adapter !== playlistWrapper
                    val vp2 = vp2 ?: rv.findParent<ViewPager2>().also { vp2 = it }
                    if (vp2.isUserInputEnabled != isNotHorizontalWrapper) {
                        vp2.isUserInputEnabled = isNotHorizontalWrapper
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isNotHorizontalWrapper) return false
                    val endX = e.x.toInt()
                    val direction = startX - endX
                    val canScrollHorizontally =
                        playlistWrapper.wrapper?.canScrollHorizontally(1)?.not()?.let { csh ->
                            if (!csh) false else direction > 0
                        } ?: true
                    val vp2 = vp2 ?: rv.findParent<ViewPager2>().also { vp2 = it }
                    if (vp2.isUserInputEnabled != canScrollHorizontally) {
                        vp2.isUserInputEnabled = canScrollHorizontally
                    }
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit

    }

    private inner class VideoIntroductionAdapter :
        BaseSingleDifferAdapter<HanimeVideo, DataBindingHolder<ItemVideoIntroductionBinding>>(
            COMPARATOR
        ), AdapterLikeDataBindingPage<ItemVideoIntroductionBinding> {

        override var binding: ItemVideoIntroductionBinding? = null

        override fun onBindViewHolder(
            holder: DataBindingHolder<ItemVideoIntroductionBinding>,
            item: HanimeVideo?,
        ) {
            item ?: return
            holder.binding.apply {
                this@VideoIntroductionAdapter.binding = this
                uploadTime.text = item.uploadTime?.format(LOCAL_DATE_FORMAT)
                views.text = getString(R.string.s_view_times, item.views.toString())
                tvIntroduction.setContent(item.introduction)
                tags.tags = item.tags
                tags.lifecycle = viewLifecycleOwner.lifecycle

                initTitle(item)
                initArtist(item.artist)
                initDownloadButton(item)
                initFunctionBar(item)
            }
        }

        override fun onBindViewHolder(
            holder: DataBindingHolder<ItemVideoIntroductionBinding>,
            item: HanimeVideo?,
            payloads: List<Any>,
        ) {
            if (payloads.isEmpty() || payloads.first() == 0)
                return super.onBindViewHolder(holder, item, payloads)
            item ?: return
            val bitset = payloads.first() as Int
            if (bitset and FAV != 0) {
                holder.binding.initFavButton(item)
            }
            // #issue-202: 加入清单之后不会正常刷新
            if (bitset and PLAYLIST != 0) {
                holder.binding.initMyList(item.myList)
            }
            if (bitset and SUBSCRIBE != 0) {
                holder.binding.initArtist(item.artist)
            }
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int,
        ): DataBindingHolder<ItemVideoIntroductionBinding> {
            return DataBindingHolder(
                ItemVideoIntroductionBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
            )
        }

        private fun ItemVideoIntroductionBinding.initTitle(info: HanimeVideo) {
            title.text = info.title.also { initShareButton(it) }
            chineseTitle.text = info.chineseTitle
            // #issue-80: 长按复制功能请求
            title.setOnLongClickListener {
                title.text.copyToClipboard()
                showShortToast(R.string.copy_to_clipboard)
                return@setOnLongClickListener true
            }
            chineseTitle.setOnLongClickListener {
                chineseTitle.text.copyToClipboard()
                showShortToast(R.string.copy_to_clipboard)
                return@setOnLongClickListener true
            }
        }

        private fun ItemVideoIntroductionBinding.initFavButton(info: HanimeVideo) {
            if (info.isFav) {
                btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_24)
                btnAddToFav.setText(R.string.liked)
            } else {
                btnAddToFav.setDrawableTop(R.drawable.ic_baseline_favorite_border_24)
                btnAddToFav.setText(R.string.add_to_fav)
            }
            // #issue-204: 收藏可能会导致重复
            // reason: 1. 在收藏时，可能会多次点击，导致多次请求
            //         2. payload 后没有重新绑定新 videoData，点击事件未更新
            btnAddToFav.clickWithCondition(viewLifecycleOwner.lifecycle, R.id.click_condition) {
                if (isAlreadyLogin) {
                    it.setTag(R.id.click_condition, false)
                    if (info.isFav) {
                        viewModel.removeFromFavVideo(
                            viewModel.videoCode,
                            info.currentUserId,
                        )
                    } else {
                        viewModel.addToFavVideo(
                            viewModel.videoCode,
                            info.currentUserId,
                        )
                    }
                } else {
                    showShortToast(R.string.login_first)
                }
            }
        }

        private fun ItemVideoIntroductionBinding.initArtist(artist: HanimeVideo.Artist?) {
            if (artist == null) {
                vgArtist.isGone = true
            } else {
                vgArtist.isGone = false
                vgArtist.setOnClickListener {
                    startActivity<SearchActivity>(
                        ADVANCED_SEARCH_MAP to advancedSearchMapOf(
                            HAdvancedSearch.QUERY to artist.name,
                            HAdvancedSearch.GENRE to artist.genre
                        )
                    )
                }
                tvArtist.text = artist.name
                tvGenre.text = artist.genre
                ivArtist.load(artist.avatarUrl) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
                btnSubscribe.isVisible = artist.post != null
                if (btnSubscribe.isVisible && artist.post != null) {
                    btnSubscribe.text = if (artist.isSubscribed) {
                        getString(R.string.subscribed)
                    } else {
                        getString(R.string.subscribe)
                    }
                    btnSubscribe.clickWithCondition(
                        viewLifecycleOwner.lifecycle, R.id.click_condition
                    ) {
                        if (isAlreadyLogin) {
                            if (artist.isSubscribed) {
                                context.showAlertDialog {
                                    setTitle(R.string.unsubscribe_artist)
                                    setMessage(R.string.sure_to_unsubscribe)
                                    setPositiveButton(R.string.sure) { _, _ ->
                                        it.setTag(R.id.click_condition, false)
                                        viewModel.unsubscribeArtist(
                                            artist.post.userId,
                                            artist.post.artistId
                                        )
                                    }
                                    setNegativeButton(R.string.no, null)
                                }
                            } else {
                                it.setTag(R.id.click_condition, false)
                                viewModel.subscribeArtist(
                                    artist.post.userId,
                                    artist.post.artistId
                                )
                            }
                        } else {
                            showShortToast(R.string.login_first)
                        }
                    }
                }
            }
        }

        private fun ItemVideoIntroductionBinding.initFunctionBar(videoData: HanimeVideo) {
            initFavButton(videoData)
            initMyList(videoData.myList)
            btnToWebpage.clickTrigger(viewLifecycleOwner.lifecycle) {
                browse(getHanimeVideoLink(viewModel.videoCode))
            }
        }

        private fun ItemVideoIntroductionBinding.initMyList(myList: HanimeVideo.MyList?) {
            btnMyList.setOnClickListener {
                if (isAlreadyLogin && myList != null && myList.myListInfo.isNotEmpty()) {
                    requireContext().showAlertDialog {
                        setTitle(R.string.add_to_playlist)
                        setMultiChoiceItems(
                            myList.titleArray,
                            myList.isSelectedArray,
                        ) { _, index, isChecked ->
                            viewModel.modifyMyList(
                                myList.myListInfo[index].code,
                                viewModel.videoCode, isChecked, index
                            )
                        }
                        setNeutralButton(R.string.back, null)
                    }
                } else {
                    showShortToast(R.string.login_first)
                }
            }
        }

        private fun ItemVideoIntroductionBinding.initShareButton(title: String) {
            val shareText =
                title + "\n" + getHanimeVideoLink(viewModel.videoCode) + "\n" + "- From Han1meViewer -"
            btnShare.setOnClickListener {
                shareText(shareText, getString(R.string.long_press_share_to_copy))
            }
            btnShare.setOnLongClickListener {
                shareText.copyToClipboard()
                showShortToast(R.string.copy_to_clipboard)
                return@setOnLongClickListener true
            }
        }

        private fun ItemVideoIntroductionBinding.initDownloadButton(videoData: HanimeVideo) {
            if (videoData.videoUrls.isEmpty()) {
                showShortToast(R.string.no_video_links_found)
            } else btnDownload.clickTrigger(viewLifecycleOwner.lifecycle) {
                XPopup.Builder(context)
                    .atView(it)
                    .asAttachList(videoData.videoUrls.keys.toTypedArray(), null) { _, key ->
                        if (key == HanimeResolution.RES_UNKNOWN) {
                            showShortToast(R.string.cannot_download_here)
                            browse(getHanimeVideoDownloadLink(viewModel.videoCode))
                        } else {
                            checkedQuality = key
                            viewModel.findDownloadedHanime(
                                viewModel.videoCode, quality = key
                            )
                        }
                    }.show()
            }
        }
    }
}