package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.FragmentHomePageBinding
import com.yenaly.han1meviewer.logic.model.HomePageModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.activity.PreviewActivity
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/12 012 12:31
 */
class HomePageFragment : YenalyFragment<FragmentHomePageBinding, MainViewModel>(),
    IToolbarFragment<MainActivity> {

    private val latestHanimeAdapter by unsafeLazy {
        HanimeVideoRvAdapter().apply {
            setDiffCallback(HanimeVideoRvAdapter.COMPARATOR)
        }
    }

    private val latestUploadAdapter by unsafeLazy {
        HanimeVideoRvAdapter().apply {
            setDiffCallback(HanimeVideoRvAdapter.COMPARATOR)
        }
    }

    private val hotHanimeMonthlyAdapter by unsafeLazy {
        HanimeVideoRvAdapter().apply {
            setDiffCallback(HanimeVideoRvAdapter.COMPARATOR)
        }
    }

    private val hanimeCurrentAdapter by unsafeLazy {
        HanimeVideoRvAdapter().apply {
            setDiffCallback(HanimeVideoRvAdapter.COMPARATOR)
        }
    }

    private val hanimeTheyWatchedAdapter by unsafeLazy {
        HanimeVideoRvAdapter().apply {
            setDiffCallback(HanimeVideoRvAdapter.COMPARATOR)
        }
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {

        initTitle()

        (activity as MainActivity).setupToolbar()

        binding.latestHanime.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = latestHanimeAdapter
        }
        binding.latestUpload.rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hanimeTheyWatchedAdapter
        }
        binding.homePageSrl.apply {
            setOnRefreshListener {
                // will enter here firstly. cuz the flow's def value is Loading.
                viewModel.getHomePage()
            }
            setEnableLoadMore(false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        lifecycleScope.launch {
            whenCreated {
                viewModel.homePageFlow.collect { state ->
                    binding.homePageNsv.isGone = state !is WebsiteState.Success
                    binding.banner.isVisible =
                        state is WebsiteState.Success || binding.banner.isVisible // 只有在刚开始的时候是不可见的
                    binding.errorTip.isVisible = state is WebsiteState.Error
                    binding.appBar.setExpanded(state is WebsiteState.Success, true)
                    when (state) {
                        is WebsiteState.Loading -> {
                            binding.homePageSrl.autoRefresh()
                            binding.homePageNsv.isGone = latestHanimeAdapter.data.isEmpty()
                        }

                        is WebsiteState.Success -> {
                            binding.homePageSrl.finishRefresh()
                            initBanner(state.info)
                            latestHanimeAdapter.setDiffNewData(state.info.latestHanime)
                            latestUploadAdapter.setDiffNewData(state.info.latestUpload)
                            hotHanimeMonthlyAdapter.setDiffNewData(state.info.hotHanimeMonthly)
                            hanimeCurrentAdapter.setDiffNewData(state.info.hanimeCurrent)
                            hanimeTheyWatchedAdapter.setDiffNewData(state.info.hanimeTheyWatched)
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

    private fun initBanner(info: HomePageModel) {
        info.banner?.let { banner ->
            binding.tvBannerTitle.text = spannable {
                banner.title.quote(Color.RED, gapWidth = 16)
            }
            binding.tvBannerDesc.text = banner.description
            binding.cover.load(banner.picUrl) {
                crossfade(true)
            }
            binding.btnBanner.setOnClickListener {
                requireActivity().startActivity<VideoActivity>(VIDEO_CODE to banner.videoCode)
            }
        }
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@HomePageFragment.binding.toolbar
        setSupportActionBar(toolbar)
        this@HomePageFragment.addMenu(R.menu.menu_main_toolbar, viewLifecycleOwner) { item ->
            when (item.itemId) {
                R.id.tb_search -> {
                    startActivity<SearchActivity>()
                    return@addMenu true
                }

                R.id.tb_previews -> {
                    startActivity<PreviewActivity>()
                    return@addMenu true
                }
            }
            return@addMenu item.onNavDestinationSelected(navController)
        }

        toolbar.setupWithMainNavController()
    }
}