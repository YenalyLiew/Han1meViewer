package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isGone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.yenaly.han1meviewer.ADVANCED_SEARCH_MAP
import com.yenaly.han1meviewer.AdvancedSearchMap
import com.yenaly.han1meviewer.HAdvancedSearch
import com.yenaly.han1meviewer.VideoCoverSize
import com.yenaly.han1meviewer.databinding.ActivitySearchBinding
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.adapter.FixedGridLayoutManager
import com.yenaly.han1meviewer.ui.adapter.HanimeSearchHistoryRvAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.fragment.search.SearchOptionsPopupFragment
import com.yenaly.han1meviewer.ui.viewmodel.SearchViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.intentExtra
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/13 013 22:29
 */
class SearchActivity : YenalyActivity<ActivitySearchBinding, SearchViewModel>(), StateLayoutMixin {

    /**
     * 判断adapter是否已经加载，防止多次加载导致滑动浏览总是跳到顶部。
     */
    private var hasAdapterLoaded = false

    private val searchAdapter by unsafeLazy { HanimeVideoRvAdapter() }

    private val advancedSearchMap by intentExtra<Any>(ADVANCED_SEARCH_MAP)

    private val optionsPopupFragment by unsafeLazy { SearchOptionsPopupFragment() }

    override fun setUiStyle() {
        // SystemStatusUtil.fullScreen(window, true)
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {
        advancedSearchMap?.let(::loadAdvancedSearch)

        initSearchBar()

        binding.state.init()

        binding.searchRv.apply {
            layoutManager = FixedGridLayoutManager(
                this@SearchActivity, VideoCoverSize.Normal.videoInOneLine
            )
            adapter = searchAdapter
            addOnScrollListener(object : OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        binding.searchBar.hideHistory()
                    }
                }
            })
        }
        binding.searchSrl.apply {
            setOnLoadMoreListener {
                getHanimeSearchResult()
            }
            setOnRefreshListener {
                // will enter here firstly. cuz the flow's def value is Loading.
                getNewHanimeSearchResult()
            }
            setDisableContentWhenRefresh(true)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.searchStateFlow.collect { state ->
                    binding.searchRv.isGone = state is PageLoadingState.Error
                    when (state) {
                        is PageLoadingState.Loading -> {
                            if (viewModel.searchFlow.value.isEmpty()) binding.searchSrl.autoRefresh()
                        }

                        is PageLoadingState.Success -> {
                            viewModel.page++
                            binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(true)
                            if (!hasAdapterLoaded) {
                                binding.searchRv.layoutManager =
                                    state.info.buildFlexibleGridLayoutManager()
                                hasAdapterLoaded = true
                            }
                            binding.state.showContent()
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.searchSrl.finishLoadMoreWithNoMoreData()
                            if (viewModel.searchFlow.value.isEmpty()) {
                                binding.state.showEmpty()
                                binding.searchRv.isGone = true
                            }
                        }

                        is PageLoadingState.Error -> {
                            binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(false)
                            // set error view
                            binding.state.showError(state.throwable)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.searchFlow.collectLatest {
                    searchAdapter.submitList(it)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val dataState = viewModel.searchStateFlow.value
        binding.searchRv.layoutManager = if (dataState is PageLoadingState.Success) {
            dataState.info.buildFlexibleGridLayoutManager()
        } else FixedGridLayoutManager(this, VideoCoverSize.Normal.videoInOneLine)
    }

    private fun getHanimeSearchResult() {
        viewModel.getHanimeSearchResult(
            viewModel.page,
            viewModel.query, viewModel.genre, viewModel.sort, viewModel.broad,
            viewModel.year, viewModel.month, viewModel.duration,
            viewModel.tagSet, viewModel.brandSet
        )
    }

    /**
     * 獲取最新結果，清除之前保存的所有數據
     */
    private fun getNewHanimeSearchResult() {
        viewModel.page = 1
        hasAdapterLoaded = false
        viewModel.clearHanimeSearchResult()
        getHanimeSearchResult()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun initSearchBar() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.searchBar.hideHistory()) {
                return@addCallback
            }
            finish()
        }
        val searchAdapter = HanimeSearchHistoryRvAdapter()
        searchAdapter.listener = object : HanimeSearchHistoryRvAdapter.OnItemViewClickListener {
            override fun onItemClickListener(v: View, history: SearchHistoryEntity?) {
                binding.searchBar.searchText = history?.query
            }

            override fun onItemRemoveListener(v: View, history: SearchHistoryEntity?) {
                history?.let(viewModel::deleteSearchHistory)
            }
        }
        binding.searchBar.apply hsb@{
            adapter = searchAdapter
            onTagClickListener = {
                optionsPopupFragment.showIn(this@SearchActivity)
            }
            onBackClickListener = {
                finish()
            }
            onSearchClickListener = { _, text ->
                viewModel.query = text
                if (text.isNotBlank()) {
                    viewModel.insertSearchHistory(SearchHistoryEntity(text))
                }
                // getNewHanimeSearchResult() 下面那個方法自動幫你執行這個方法了
                binding.searchSrl.autoRefresh()
            }

            // 搜索框文字改变走这里
            textChangeFlow()
                .debounce(300)
                .flatMapLatest {
                    viewModel.loadAllSearchHistories(it)
                }.flowOn(Dispatchers.IO).onEach {
                    this@hsb.history = it
                }.flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        }
    }

    private fun List<HanimeInfo>.buildFlexibleGridLayoutManager(): GridLayoutManager {
        val counts = if (any { it.itemType == HanimeInfo.NORMAL })
            VideoCoverSize.Normal.videoInOneLine else VideoCoverSize.Simplified.videoInOneLine
        return FixedGridLayoutManager(this@SearchActivity, counts)
    }

    /**
     * 分析该 any 并加载给相应 ViewModel 中的参数
     *
     * any 可以爲 [AdvancedSearchMap] 或者 [String]
     *
     * 若为 [String]，则默认给 query 处理
     */
    @Suppress("UNCHECKED_CAST")
    private fun loadAdvancedSearch(any: Any) {
        if (any is String) {
            viewModel.query = any
            binding.searchBar.searchText = any
            return
        }
        val map = any as AdvancedSearchMap
        (map[HAdvancedSearch.QUERY] as? String)?.let {
            viewModel.query = it
            binding.searchBar.searchText = it
        }
        viewModel.genre = map[HAdvancedSearch.GENRE] as? String
        viewModel.sort = map[HAdvancedSearch.SORT] as? String
        viewModel.year = map[HAdvancedSearch.YEAR] as? Int
        viewModel.month = map[HAdvancedSearch.MONTH] as? Int
        viewModel.duration = map[HAdvancedSearch.DURATION] as? String

        when (val tags = map[HAdvancedSearch.TAGS]) {
            is String -> viewModel.tagSet.add(tags)
            is HashSet<*> -> viewModel.tagSet.addAll(tags as Set<String>)
        }
        when (val brands = map[HAdvancedSearch.BRANDS]) {
            is String -> viewModel.brandSet.add(brands)
            is HashSet<*> -> viewModel.brandSet.addAll(brands as Set<String>)
        }
    }
}