package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_IN_ONE_LINE_LANDSCAPE
import com.yenaly.han1meviewer.VIDEO_IN_ONE_LINE_PORTRAIT
import com.yenaly.han1meviewer.databinding.FragmentHomePageBinding
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.isOrientationLandscape
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/12 012 12:31
 */
class HomePageFragment : YenalyFragment<FragmentHomePageBinding, MainViewModel>() {

    private val latestHanimeAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    private val latestUploadAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    private val hotHanimeMonthlyAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    private val hanimeCurrentAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    private val hanimeTheyWatchedAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {

        (activity as? MainActivity)?.setToolbarSubtitle(null)

        initTitle()

        binding.latestHanime.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = latestHanimeAdapter
        }
        binding.latestUpload.rv.apply {
            layoutManager = GridLayoutManager(
                context,
                if (isOrientationLandscape) VIDEO_IN_ONE_LINE_LANDSCAPE else VIDEO_IN_ONE_LINE_PORTRAIT
            )
            adapter = latestUploadAdapter
        }
        binding.hotHanimeMonthly.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hotHanimeMonthlyAdapter
        }
        binding.hanimeCurrent.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hanimeCurrentAdapter
        }
        binding.hanimeTheyWatched.rv.apply {
            layoutManager = GridLayoutManager(
                context,
                if (isOrientationLandscape) VIDEO_IN_ONE_LINE_LANDSCAPE else VIDEO_IN_ONE_LINE_PORTRAIT
            )
            adapter = hanimeTheyWatchedAdapter
        }
        binding.homePageSrl.apply {
            setOnRefreshListener {
                // will enter here firstly. cuz the flow's def value is Loading.
                getHomePage()
            }
            setEnableLoadMore(false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun liveDataObserve() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.homePageFlow.collect { state ->
                    binding.homePageNsv.isGone = state !is WebsiteState.Success
                    binding.errorTip.isVisible = state is WebsiteState.Error
                    when (state) {
                        is WebsiteState.Loading -> {
                            binding.homePageSrl.autoRefresh()
                        }
                        is WebsiteState.Success -> {
                            binding.homePageSrl.finishRefresh()
                            latestHanimeAdapter.setList(state.info.latestHanime)
                            latestUploadAdapter.setList(state.info.latestUpload)
                            hotHanimeMonthlyAdapter.setList(state.info.hotHanimeMonthly)
                            hanimeCurrentAdapter.setList(state.info.hanimeCurrent)
                            hanimeTheyWatchedAdapter.setList(state.info.hanimeTheyWatched)
                        }
                        is WebsiteState.Error -> {
                            binding.homePageSrl.finishRefresh()
                            binding.errorTip.text = "🥺\n${state.throwable.message}"
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.latestUpload.rv.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_PORTRAIT)
                binding.hanimeTheyWatched.rv.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_PORTRAIT)
            }
            else -> {
                binding.latestUpload.rv.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_LANDSCAPE)
                binding.hanimeTheyWatched.rv.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_LANDSCAPE)
            }
        }
    }

    private fun initTitle() {
        binding.latestHanime.title.setText(R.string.latest_hanime)
        binding.latestHanime.subTitle.setText(R.string.h_anime)

        binding.latestUpload.title.setText(R.string.latest_upload)
        binding.latestUpload.subTitle.setText(R.string.fresh)

        binding.hotHanimeMonthly.title.setText(R.string.hot_video)
        binding.hotHanimeMonthly.subTitle.setText(R.string.this_month)

        binding.hanimeCurrent.title.setText(R.string.hot_video_2)
        binding.hanimeCurrent.subTitle.setText(R.string.current)

        binding.hanimeTheyWatched.title.setText(R.string.they_watched)
        binding.hanimeTheyWatched.subTitle.setText(R.string.trends)
    }

    private fun getHomePage() = viewModel.getHomePage()
}