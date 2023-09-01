package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.databinding.ActivitySearchBinding
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.model.HanimeInfoModel
import com.yenaly.han1meviewer.logic.state.PageLoadingState
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/13 013 22:29
 */
class SearchActivity : YenalyActivity<ActivitySearchBinding, SearchViewModel>() {

    private val searchAdapter by unsafeLazy { HanimeVideoRvAdapter() }
    private val fromVideoTag by intentExtra<String>(FROM_VIDEO_TAG)

    private val optionsPopupFragment by unsafeLazy { SearchOptionsPopupFragment() }

    override fun setUiStyle() {
        // SystemStatusUtil.fullScreen(window, true)
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {
        fromVideoTag?.let {
            viewModel.query = it
            binding.searchBar.searchText = it
        }

        initSearchBar()

        binding.searchRv.apply {
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
            whenStarted {
                viewModel.searchFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Loading -> {
                            // 防止只要list為空就會蹦出來empty view，這樣觀感不好
                            searchAdapter.removeEmptyView()
                            if (searchAdapter.data.isEmpty()) binding.searchSrl.autoRefresh()
                        }

                        is PageLoadingState.Success -> {
                            viewModel.page++
                            if (binding.searchSrl.isRefreshing) binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(true)
                            searchAdapter.addData(state.info)
                            binding.searchRv.layoutManager = buildFlexibleGridLayoutManager()
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.searchSrl.finishLoadMoreWithNoMoreData()
                            if (searchAdapter.data.isEmpty()) searchAdapter.setEmptyView(R.layout.layout_empty_view)
                        }

                        is PageLoadingState.Error -> {
                            if (binding.searchSrl.isRefreshing) binding.searchSrl.finishRefresh()
                            binding.searchSrl.finishLoadMore(false)
                            // set error view
                            val errView = LayoutInflater.from(this@SearchActivity).inflate(
                                R.layout.layout_empty_view,
                                searchAdapter.recyclerViewOrNull,
                                false
                            )
                            errView.findViewById<TextView>(R.id.tv_empty).text =
                                "🥺\n${state.throwable.message}"
                            searchAdapter.setEmptyView(errView)
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.searchRv.layoutManager = buildFlexibleGridLayoutManager()
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
        searchAdapter.data.clear()
        viewModel.page = 1
        getHanimeSearchResult()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun initSearchBar() {
        val searchAdapter = HanimeSearchHistoryRvAdapter()
        searchAdapter.setDiffCallback(HanimeSearchHistoryRvAdapter.COMPARATOR)
        searchAdapter.listener = object : HanimeSearchHistoryRvAdapter.OnItemViewClickListener {
            override fun onItemClickListener(v: View, history: SearchHistoryEntity) {
                binding.searchBar.searchText = history.query
            }

            override fun onItemRemoveListener(v: View, history: SearchHistoryEntity) {
                viewModel.deleteSearchHistory(history)
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

    private fun buildFlexibleGridLayoutManager(): GridLayoutManager {
        val counts = if (searchAdapter.getItemViewType(0) == HanimeInfoModel.NORMAL)
            VIDEO_IN_ONE_LINE else SIMPLIFIED_VIDEO_IN_ONE_LINE
        return GridLayoutManager(this, counts)
    }
}