package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
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
import com.yenaly.han1meviewer.logic.model.SearchOption
import com.yenaly.han1meviewer.logic.model.SearchOption.Companion.flatten
import com.yenaly.han1meviewer.logic.model.SearchOption.Companion.get
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.adapter.FixedGridLayoutManager
import com.yenaly.han1meviewer.ui.adapter.HanimeSearchHistoryRvAdapter
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.fragment.search.HMultiChoicesDialog
import com.yenaly.han1meviewer.ui.fragment.search.SearchOptionsPopupFragment
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.ui.viewmodel.SearchViewModel
import com.yenaly.han1meviewer.util.logScreenViewEvent
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.dp
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
class SearchActivity : YenalyActivity<ActivitySearchBinding>(), StateLayoutMixin {

    val viewModel by viewModels<SearchViewModel>()
    val myListViewModel by viewModels<MyListViewModel>()

    val subscribeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                initSubscription()
            }
        }

    /**
     * 判断adapter是否已经加载，防止多次加载导致滑动浏览总是跳到顶部。
     */
    private var hasAdapterLoaded = false

    private val searchAdapter by unsafeLazy { HanimeVideoRvAdapter() }
    private val historyAdapter by unsafeLazy { HanimeSearchHistoryRvAdapter() }

    private val advancedSearchMap by intentExtra<Any>(ADVANCED_SEARCH_MAP)

    private val optionsPopupFragment by unsafeLazy { SearchOptionsPopupFragment() }

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivitySearchBinding =
        ActivitySearchBinding.inflate(layoutInflater)

    override val onFragmentResumedListener: (Fragment) -> Unit = { fragment ->
        logScreenViewEvent(fragment)
    }

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {
        advancedSearchMap?.let(::loadAdvancedSearch)

        initSearchBar()
        initSubscription()

        binding.state.init()

        binding.searchRv.apply {
            layoutManager = FixedGridLayoutManager(
                this@SearchActivity, VideoCoverSize.Normal.videoInOneLine
            )
            adapter = searchAdapter
            clipToPadding = false
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.searchBar) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchRv) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = sysBars.bottom, top = sysBars.top + 68.dp)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchHeader) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updateLayoutParams<MarginLayoutParams> {
                topMargin = sysBars.top + 68.dp
            }
            WindowInsetsCompat.CONSUMED
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
        Log.d("SearchActivity", buildString {
            appendLine("page: ${viewModel.page}, query: ${viewModel.query}, genre: ${viewModel.genre}, ")
            appendLine("sort: ${viewModel.sort}, broad: ${viewModel.broad}, year: ${viewModel.year}, ")
            appendLine("month: ${viewModel.month}, duration: ${viewModel.duration}, ")
            appendLine("tagMap: ${viewModel.tagMap}, brandMap: ${viewModel.brandMap}")
        })
        viewModel.getHanimeSearchResult(
            viewModel.page,
            viewModel.query, viewModel.genre, viewModel.sort, viewModel.broad,
            viewModel.year, viewModel.month, viewModel.duration,
            viewModel.tagMap.flatten(), viewModel.brandMap.flatten()
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
        historyAdapter.listener = object : HanimeSearchHistoryRvAdapter.OnItemViewClickListener {
            override fun onItemClickListener(v: View, history: SearchHistoryEntity?) {
                binding.searchBar.searchText = history?.query
            }

            override fun onItemRemoveListener(v: View, history: SearchHistoryEntity?) {
                history?.let(viewModel::deleteSearchHistory)
            }
        }
        binding.searchBar.apply hsb@{
            historyAdapter = this@SearchActivity.historyAdapter
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
                    this@SearchActivity.historyAdapter.submitList(it)
                }.flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        }
    }

    private fun initSubscription() {
        myListViewModel.subscription.getSubscriptionsWithSinglePage()
    }

    private fun List<HanimeInfo>.buildFlexibleGridLayoutManager(): GridLayoutManager {
        val counts = if (any { it.itemType == HanimeInfo.NORMAL })
            VideoCoverSize.Normal.videoInOneLine else VideoCoverSize.Simplified.videoInOneLine
        return FixedGridLayoutManager(this@SearchActivity, counts)
    }

    fun setSearchText(text: String?, canTextChange: Boolean = true) {
        viewModel.query = text
        binding.searchBar.searchText = text
        binding.searchBar.canTextChange = canTextChange
    }

    val searchText: String? get() = binding.searchBar.searchText

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
            setSearchText(any)
            return
        }
        val map = any as AdvancedSearchMap
        (map[HAdvancedSearch.QUERY] as? String)?.let {
            setSearchText(it)
        }
        viewModel.genre = map[HAdvancedSearch.GENRE] as? String
        viewModel.sort = map[HAdvancedSearch.SORT] as? String
        viewModel.year = map[HAdvancedSearch.YEAR] as? Int
        viewModel.month = map[HAdvancedSearch.MONTH] as? Int
        viewModel.duration = map[HAdvancedSearch.DURATION] as? String

        when (val tags = map[HAdvancedSearch.TAGS]) {
            is Map<*, *> -> {
                val tagMap = tags as Map<Int, *>
                tagMap.forEach { (k, v) ->
                    val scope = viewModel.tags[k]
                    when (v) {
                        is String -> {
                            val option = scope.find { it.searchKey == v }
                            viewModel.tagMap[k] = option?.let(::setOf) ?: emptySet()
                        }

                        is Set<*> -> {
                            val keySet = scope.filterTo(mutableSetOf()) { it.searchKey in v }
                            viewModel.tagMap[k] = keySet
                        }
                    }
                }
            }

            // 不推荐使用
            is String -> {
                kotlin.run t@{
                    viewModel.tags.forEach { (k, v) ->
                        v.find { it.searchKey == tags }?.let { so ->
                            val scopeKey = SearchOption.toScopeKey(k)
                            viewModel.tagMap[scopeKey] = setOf(so)
                            return@t
                        }
                    }
                }
            }

            // 不推荐使用
            is Set<*> -> {
                viewModel.tags.forEach { (k, v) ->
                    val keySet = v.filterTo(mutableSetOf()) { it.searchKey in tags }
                    viewModel.tagMap[SearchOption.toScopeKey(k)] = keySet
                }
            }
        }
        when (val brands = map[HAdvancedSearch.BRANDS]) {
            is String -> {
                val brand = viewModel.brands.find { it.searchKey == brands }
                viewModel.brandMap[HMultiChoicesDialog.UNKNOWN_ADAPTER] =
                    brand?.let(::setOf) ?: emptySet()
            }

            is Set<*> -> {
                val keySet = viewModel.brands.filterTo(mutableSetOf()) { it.searchKey in brands }
                viewModel.brandMap[HMultiChoicesDialog.UNKNOWN_ADAPTER] = keySet
            }
        }
    }
}