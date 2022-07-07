package com.yenaly.han1meviewer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.databinding.FragmentVideoIntroductionBinding
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.VideoViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.isOrientationLandscape
import com.yenaly.yenaly_libs.utils.shareText
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.clickTrigger
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 21:09
 */
class VideoIntroductionFragment :
    YenalyFragment<FragmentVideoIntroductionBinding, VideoViewModel>() {

    private var csrfToken: String? = null
    private var currentUserId: String? = null

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
        binding.relatedHanime.rv.layoutManager = GridLayoutManager(
            context,
            if (isOrientationLandscape) VIDEO_IN_ONE_LINE_LANDSCAPE else VIDEO_IN_ONE_LINE_PORTRAIT
        )
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
                            csrfToken = state.info.csrfToken.also {
                                Log.d("csrf_token", it.toString())
                            }
                            currentUserId = state.info.currentUserId.also {
                                Log.d("current_user_id", it.toString())
                            }
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
                        }
                        else -> {}
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
                        is WebsiteState.Loading -> {
                        }
                        is WebsiteState.Success -> {
                            showShortToast("喜愛成功")
                        }
                    }
                }
            }
        }
    }

    private fun initFunctionBar() {
        binding.btnAddToFav.clickTrigger(viewLifecycleOwner.lifecycle) {
            if (alreadyLogin) {
                viewModel.addToFavVideo(viewModel.videoCode, currentUserId, csrfToken)
            } else {
                showShortToast("請先登入！")
            }
        }
        binding.btnWatchLater.clickTrigger(viewLifecycleOwner.lifecycle) {
            // todo
        }
    }

    private fun initShareButton(title: String) {
        binding.btnShare.setOnClickListener {
            shareText(title + "\n" + getHanimeVideoLink(viewModel.videoCode) + "\n" + "From Han1meViewer")
        }
    }
}