package com.yenaly.han1meviewer.ui.fragment.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VideoCoverSize
import com.yenaly.han1meviewer.databinding.FragmentPageListBinding
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeMyListVideoAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.fragment.LoginNeededFragmentMixin
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:43
 */
class MyFavVideoFragment : YenalyFragment<FragmentPageListBinding>(),
    IToolbarFragment<MainActivity>, LoginNeededFragmentMixin, StateLayoutMixin {

    val viewModel by activityViewModels<MyListViewModel>()

    private var page: Int
        set(value) {
            viewModel.fav.favVideoPage = value
        }
        get() = viewModel.fav.favVideoPage

    private val adapter by unsafeLazy { HanimeMyListVideoAdapter() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPageListBinding {
        return FragmentPageListBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        checkLogin()
        (activity as MainActivity).setupToolbar()

        binding.state.init()

        adapter.setOnItemLongClickListener { _, _, position ->
            val item = adapter.getItem(position) ?: return@setOnItemLongClickListener true
            requireContext().showAlertDialog {
                setTitle(R.string.delete_fav)
                setMessage(getString(R.string.sure_to_delete_s, item.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.fav.deleteMyFavVideo(item.videoCode, position)
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }

        binding.rvPageList.apply {
            layoutManager = GridLayoutManager(context, VideoCoverSize.Simplified.videoInOneLine)
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fav.favVideoStateFlow.collect { state ->
                    when (state) {
                        is PageLoadingState.Error -> {
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(false)
                            // set error view
                            binding.state.showError(state.throwable)
                        }

                        is PageLoadingState.Loading -> {
                            adapter.stateView = null
                            if (viewModel.fav.favVideoFlow.value.isEmpty()) binding.srlPageList.autoRefresh()
                        }

                        is PageLoadingState.NoMoreData -> {
                            binding.srlPageList.finishLoadMoreWithNoMoreData()
                            if (viewModel.fav.favVideoFlow.value.isEmpty()) binding.state.showEmpty()
                        }

                        is PageLoadingState.Success -> {
                            page++
                            binding.srlPageList.finishRefresh()
                            binding.srlPageList.finishLoadMore(true)
                            binding.state.showContent()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fav.favVideoFlow.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fav.deleteMyFavVideoFlow.collect { state ->
                when (state) {
                    is WebsiteState.Error -> {
                        showShortToast(R.string.delete_failed)
                        state.throwable.printStackTrace()
                    }

                    is WebsiteState.Loading -> {
                    }

                    is WebsiteState.Success -> {
                        showShortToast(R.string.delete_success)
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.rvPageList.layoutManager =
            GridLayoutManager(context, VideoCoverSize.Simplified.videoInOneLine)
    }

    private fun getMyFavVideo() {
        viewModel.fav.getMyFavVideoItems(page)
    }

    private fun getNewMyFavVideo() {
        page = 1
        viewModel.fav.clearMyListItems()
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
                        setTitle(R.string.attention)
                        setMessage(R.string.long_press_to_cancel_fav)
                        setPositiveButton(R.string.ok, null)
                    }
                    return@addMenu true
                }
            }
            return@addMenu false
        }

        toolbar.setupWithMainNavController()
    }
}