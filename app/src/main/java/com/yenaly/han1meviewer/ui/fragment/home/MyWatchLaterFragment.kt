package com.yenaly.han1meviewer.ui.fragment.home

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_IN_ONE_LINE_LANDSCAPE
import com.yenaly.han1meviewer.VIDEO_IN_ONE_LINE_PORTRAIT
import com.yenaly.han1meviewer.databinding.FragmentPageListBinding
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.toVideoCode
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeVideoRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.isOrientationLandscape
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.showSnackBar
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:42
 */
class MyWatchLaterFragment : YenalyFragment<FragmentPageListBinding, MyListViewModel>() {

    private var page: Int = 1
    private var csrfToken: String? = null

    private val adapter by unsafeLazy { HanimeVideoRvAdapter() }

    override fun initData(savedInstanceState: Bundle?) {

        (activity as? MainActivity)?.setToolbarSubtitle(getString(R.string.watch_later))
        setHasOptionsMenu(true)

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(
                context,
                if (isOrientationLandscape) {
                    VIDEO_IN_ONE_LINE_LANDSCAPE
                } else {
                    VIDEO_IN_ONE_LINE_PORTRAIT
                }
            )
            adapter = this@MyWatchLaterFragment.adapter
        }

        binding.srlPageList.apply {
            setOnLoadMoreListener {
                getMyWatchLater()
            }
            setOnRefreshListener {
                getNewMyWatchLater()
            }
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val data = adapter.getItem(position)
                adapter.remove(data)
                // todo: strings.xml
                showSnackBar("你正在刪除該記錄", Snackbar.LENGTH_LONG) {
                    setAction("撤銷") {
                        adapter.addData(position, data)
                    }
                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deleteMyFavVideo(
                                    data.redirectLink.toVideoCode(),
                                    csrfToken
                                )
                            }
                        }
                    })
                }
            }
        }).attachToRecyclerView(binding.rvPageList)
    }

    override fun liveDataObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.watchLaterFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Error -> {
                            if (binding.srlPageList.isRefreshing) binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            val errView = LayoutInflater.from(context).inflate(
                                R.layout.layout_empty_view,
                                adapter.recyclerViewOrNull,
                                false
                            )
                            errView.findViewById<TextView>(R.id.tv_empty).text =
                                "🥺\n${state.throwable.message}"
                            adapter.setEmptyView(errView)
                        }
                        is PageLoadingState.Loading -> {
                            adapter.removeEmptyView()
                            if (adapter.data.isEmpty()) binding.srlPageList.autoRefresh()
                        }
                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            if (adapter.data.isEmpty()) adapter.setEmptyView(R.layout.layout_empty_view)
                        }
                        is PageLoadingState.Success -> {
                            page++
                            if (binding.srlPageList.isRefreshing) binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(true)
                            csrfToken = state.info.csrfToken
                            Log.d("csrf_token", csrfToken.toString())
                            adapter.addData(state.info.hanimeInfo)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteMyFavVideoFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast("刪除失敗！")
                        state.throwable.printStackTrace()
                    }
                    is WebsiteState.Loading -> {
                    }
                    is WebsiteState.Success -> {
                        showShortToast("刪除成功！")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.rvPageList.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_PORTRAIT)
            }
            else -> {
                binding.rvPageList.layoutManager =
                    GridLayoutManager(context, VIDEO_IN_ONE_LINE_LANDSCAPE)
            }
        }
    }

    private fun getMyWatchLater() {
        viewModel.getMyWatchLater(page)
    }

    private fun getNewMyWatchLater() {
        page = 1
        adapter.data.clear()
        getMyWatchLater()
    }
}