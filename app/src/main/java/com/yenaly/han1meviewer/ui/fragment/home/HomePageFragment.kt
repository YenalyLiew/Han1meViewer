package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.ui.onNavDestinationSelected
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.yenaly.han1meviewer.ADVANCED_SEARCH_MAP
import com.yenaly.han1meviewer.AdvancedSearchMap
import com.yenaly.han1meviewer.HAdvancedSearch
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.advancedSearchMapOf
import com.yenaly.han1meviewer.databinding.FragmentHomePageBinding
import com.yenaly.han1meviewer.logic.model.HomePage
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.activity.PreviewActivity
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.adapter.RvWrapper.Companion.wrappedWith
import com.yenaly.han1meviewer.ui.adapter.VideoColumnTitleAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.han1meviewer.util.addUpdateListener
import com.yenaly.han1meviewer.util.colorTransition
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/12 012 12:31
 */
class HomePageFragment : YenalyFragment<FragmentHomePageBinding, MainViewModel>(),
    IToolbarFragment<MainActivity>, StateLayoutMixin {

    companion object {
        private val animInterpolator = FastOutSlowInInterpolator()
        private val animDuration = 300L
    }

    private val latestHanimeAdapter = HanimeVideoRvAdapter()
    private val latestReleaseAdapter = HanimeVideoRvAdapter()
    private val latestUploadAdapter = HanimeVideoRvAdapter()
    private val chineseSubtitleAdapter = HanimeVideoRvAdapter()
    private val hanimeTheyWatchedAdapter = HanimeVideoRvAdapter()
    private val hanimeCurrentAdapter = HanimeVideoRvAdapter()
    private val hotHanimeMonthlyAdapter = HanimeVideoRvAdapter()

    private val concatAdapter = ConcatAdapter(
        VideoColumnTitleAdapter(R.string.latest_hanime).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.GENRE to "裏番"))
            }
        },
        latestHanimeAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.latest_release).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.SORT to "最新上市"))
            }
        },
        latestReleaseAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.latest_upload).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.SORT to "最新上傳"))
            }
        },
        latestUploadAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.chinese_subtitle).apply {
            onMoreHanimeListener = {
                toSearchActivity(
                    advancedSearchMapOf(
                        HAdvancedSearch.TAGS to "中文字幕",
                        HAdvancedSearch.SORT to "最新上傳"
                    )
                )
            }
        },
        chineseSubtitleAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.they_watched).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.SORT to "他們在看"))
            }
        },
        hanimeTheyWatchedAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.ranking_today).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.SORT to "本日排行"))
            }
        },
        hanimeCurrentAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        },
        VideoColumnTitleAdapter(R.string.ranking_this_month).apply {
            onMoreHanimeListener = {
                toSearchActivity(advancedSearchMapOf(HAdvancedSearch.SORT to "本月排行"))
            }
        },
        hotHanimeMonthlyAdapter.wrappedWith {
            LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        }
    )

    /**
     * 用於判斷是否需要 setExpanded，防止重複喚出 AppBar
     */
    private var isAfterRefreshing = false

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {

        (activity as MainActivity).setupToolbar()
        binding.state.init()

        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = concatAdapter
        binding.homePageSrl.apply {
            setOnRefreshListener {
                isAfterRefreshing = false
                // will enter here firstly. cuz the flow's def value is Loading.
                viewModel.getHomePage()
            }
            setEnableLoadMore(false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.homePageFlow.collect { state ->
                    binding.rv.isGone = state !is WebsiteState.Success
                    binding.banner.isVisible =
                        state is WebsiteState.Success || binding.banner.isVisible // 只有在刚开始的时候是不可见的
                    if (!isAfterRefreshing) {
                        binding.appBar.setExpanded(state is WebsiteState.Success, true)
                    }
                    when (state) {
                        is WebsiteState.Loading -> {
                            binding.homePageSrl.autoRefresh()
                            binding.rv.isGone = latestHanimeAdapter.items.isEmpty()
                        }

                        is WebsiteState.Success -> {
                            isAfterRefreshing = true
                            binding.homePageSrl.finishRefresh()
                            initBanner(state.info)
                            latestHanimeAdapter.submitList(state.info.latestHanime)
                            latestUploadAdapter.submitList(state.info.latestUpload)
                            hotHanimeMonthlyAdapter.submitList(state.info.hotHanimeMonthly)
                            hanimeCurrentAdapter.submitList(state.info.hanimeCurrent)
                            hanimeTheyWatchedAdapter.submitList(state.info.hanimeTheyWatched)
                            latestReleaseAdapter.submitList(state.info.latestRelease)
                            chineseSubtitleAdapter.submitList(state.info.chineseSubtitle)
                            binding.state.showContent()
                        }

                        is WebsiteState.Error -> {
                            binding.homePageSrl.finishRefresh()
                            binding.state.showError(state.throwable)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rv.adapter = null
        super.onDestroyView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun initBanner(info: HomePage) {
        info.banner?.let { banner ->
            binding.tvBannerTitle.text = banner.title
            binding.tvBannerDesc.text = banner.description
            binding.cover.load(banner.picUrl) {
                crossfade(true)
                allowHardware(false)
                target(
                    onStart = binding.cover::setImageDrawable,
                    onError = binding.cover::setImageDrawable,
                    onSuccess = {
                        binding.cover.setImageDrawable(it)
                        it.toBitmapOrNull()?.let(Palette::Builder)?.generate { p ->
                            p?.let(::handlePalette)
                        }
                    }
                )
            }
            binding.btnBanner.isEnabled = banner.videoCode != null
            binding.btnBanner.setOnClickListener {
                banner.videoCode?.let { videoCode ->
                    requireActivity().startActivity<VideoActivity>(VIDEO_CODE to videoCode)
                }
            }
        }
    }

    // #issue-160: 修复字段销毁后调用引发的错误
    private fun handlePalette(p: Palette) {
        bindingOrNull?.let { binding ->
            val darkVibrant = p.getDarkVibrantColor(Color.RED)
            val lightVibrant = p.getLightVibrantColor(Color.RED)

            val darkVibrantForContentScrim =
                p.darkVibrantSwatch?.rgb ?: p.darkMutedSwatch?.rgb ?: p.lightVibrantSwatch?.rgb
                ?: p.lightMutedSwatch?.rgb ?: Color.BLACK
            binding.collapsingToolbar.setContentScrimColor(darkVibrantForContentScrim)
            colorTransition(
                fromColor = binding.btnBanner.iconTint.defaultColor,
                toColor = darkVibrant
            ) {
                interpolator = animInterpolator
                duration = animDuration
                addUpdateListener(viewLifecycleOwner.lifecycle) {
                    val color = it.animatedValue as Int
                    binding.btnBanner.iconTint = ColorStateList.valueOf(color)
                }
            }
            colorTransition(
                fromColor = (binding.aColor.background as ColorDrawable).color,
                toColor = lightVibrant
            ) {
                interpolator = animInterpolator
                duration = animDuration
                addUpdateListener(viewLifecycleOwner.lifecycle) {
                    val color = it.animatedValue as Int
                    binding.aColor.setBackgroundColor(color)
                }
            }
        }
    }

    private fun toSearchActivity(advancedSearchMap: AdvancedSearchMap) {
        startActivity<SearchActivity>(ADVANCED_SEARCH_MAP to advancedSearchMap)
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