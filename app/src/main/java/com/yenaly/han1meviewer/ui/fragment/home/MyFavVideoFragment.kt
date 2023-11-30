package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
 * @time 2022/07/04 004 22:43
 */
class MyFavVideoFragment : YenalyFragment<FragmentPageListBinding, MyListViewModel>(),
    IToolbarFragment<MainActivity>, LoginNeededFragmentMixin, StateLayoutMixin {

    private var page: Int
        set(value) {
            viewModel.favVideoPage = value
        }
        get() = viewModel.favVideoPage

    private val adapter by unsafeLazy { HanimeMyListVideoAdapter() }

    override fun initData(savedInstanceState: Bundle?) {
        checkLogin()
        (activity as MainActivity).setupToolbar()

        binding.state.init()

        getNewMyFavVideo()

        adapter.setOnItemLongClickListener { _, _, position ->
            val item = adapter.getItem(position).notNull()
            requireContext().showAlertDialog {
                setTitle("刪除喜歡")
                setMessage(getString(R.string.sure_to_delete_s_video, item.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.deleteMyFavVideo(item.videoCode, position)
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(context, SIMPLIFIED_VIDEO_IN_ONE_LINE)
            adapter = this@MyFavVideoFragment.adapter
        }

        binding.srlPageList.apply {
            setOnLoadMoreListener {
                getMyFavVideo()
            }
            setOnRefreshListener {
                getNewMyFavVideo()
            }
            setDisableContentWhenRefresh(true)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.favVideoFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Error -> {
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            binding.state.showError(state.throwable)
                        }

                        is PageLoadingState.Loading -> {
                            adapter.stateView = null
                            if (adapter.items.isEmpty()) binding.srlPageList.autoRefreshAnimationOnly()
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            Log.d("empty", adapter.items.isEmpty().toString())
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
            viewModel.deleteMyFavVideoFlow.collect { state ->
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

    private fun getMyFavVideo() {
        viewModel.getMyFavVideoItems(page)
    }

    private fun getNewMyFavVideo() {
        page = 1
        adapter.items = emptyList()
        getMyFavVideo()
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@MyFavVideoFragment.binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setSubtitle(R.string.fav_video)
        this@MyFavVideoFragment.addMenu(
            R.menu.menu_my_list_toolbar,
            viewLifecycleOwner
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.tb_help -> {
                    requireContext().showAlertDialog {
                        setTitle("使用注意！")
                        setMessage("长按可以取消喜歡！")
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