package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.GridLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.SIMPLIFIED_VIDEO_IN_ONE_LINE
import com.yenaly.han1meviewer.databinding.FragmentPageListBinding
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeMyListVideoAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.fragment.LoginNeededFragmentMixin
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.util.notNull
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:42
 */
class MyWatchLaterFragment : YenalyFragment<FragmentPageListBinding, MyListViewModel>(),
    IToolbarFragment<MainActivity>, LoginNeededFragmentMixin, StateLayoutMixin {

    private var page: Int
        set(value) {
            viewModel.watchLaterPage = value
        }
        get() = viewModel.watchLaterPage

    private val adapter by unsafeLazy { HanimeMyListVideoAdapter() }

    override fun initData(savedInstanceState: Bundle?) {
        checkLogin()
        (activity as MainActivity).setupToolbar()
        binding.state.init()

        getNewMyWatchLater()

        adapter.setOnItemLongClickListener { _, _, position ->
            val item = adapter.getItem(position).notNull()
            requireContext().showAlertDialog {
                setTitle("刪除待看")
                setMessage(getString(R.string.sure_to_delete_s_video, item.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.deleteMyWatchLater(item.videoCode, position)
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(context, SIMPLIFIED_VIDEO_IN_ONE_LINE)
            adapter = this@MyWatchLaterFragment.adapter
        }

        binding.srlPageList.apply {
            setOnLoadMoreListener {
                getMyWatchLater()
            }
            setOnRefreshListener {
                getNewMyWatchLater()
            }
            setDisableContentWhenRefresh(true)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.watchLaterFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Error -> {
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            binding.state.showError()
                        }

                        is PageLoadingState.Loading -> {
                            adapter.stateView = null
                            if (adapter.items.isEmpty()) binding.srlPageList.autoRefreshAnimationOnly()
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            if (adapter.items.isEmpty()) binding.state.showEmpty()
                        }

                        is PageLoadingState.Success -> {
                            page++
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(true)
                            viewModel.csrfToken = state.info.csrfToken
                            adapter.addAll(state.info.hanimeInfo)
                            binding.state.showContent()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteMyWatchLaterFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast("刪除失敗！")
                        state.throwable.printStackTrace()
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        val index = state.info
                        showShortToast("刪除成功！")
                        adapter.removeAt(index)
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.rvPageList.layoutManager = GridLayoutManager(context, SIMPLIFIED_VIDEO_IN_ONE_LINE)
    }

    private fun getMyWatchLater() {
        viewModel.getMyWatchLaterItems(page)
    }

    private fun getNewMyWatchLater() {
        page = 1
        adapter.items = emptyList()
        getMyWatchLater()
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@MyWatchLaterFragment.binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setSubtitle(R.string.watch_later)
        this@MyWatchLaterFragment.addMenu(
            R.menu.menu_my_list_toolbar,
            viewLifecycleOwner
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.tb_help -> {
                    requireContext().showAlertDialog {
                        setTitle("使用注意！")
                        setMessage("长按可以取消待看！")
                        setPositiveButton("OK", null)
                    }
                    return@addMenu true
                }
            }
            return@addMenu false
        }

        toolbar.setupWithMainNavController()
    }
}