package com.yenaly.han1meviewer.ui.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentPageListBinding
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.WatchHistoryRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.han1meviewer.util.setStateViewLayout
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 21:23
 */
class WatchHistoryFragment : YenalyFragment<FragmentPageListBinding>(),
    IToolbarFragment<MainActivity>, StateLayoutMixin {

    val viewModel by activityViewModels<MainViewModel>()

    private val historyAdapter by unsafeLazy { WatchHistoryRvAdapter() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPageListBinding {
        return FragmentPageListBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        (activity as MainActivity).setupToolbar()

        binding.rvPageList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
        binding.srlPageList.finishRefreshWithNoMoreData()
        historyAdapter.setStateViewLayout(R.layout.layout_empty_view)
        historyAdapter.setOnItemLongClickListener { _, _, position ->
            val data = historyAdapter.getItem(position) ?: return@setOnItemLongClickListener true
            requireContext().showAlertDialog {
                setTitle(R.string.delete_history)
                setMessage(getString(R.string.sure_to_delete_s, data.title))
                setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.deleteWatchHistory(data)
                }
                setNegativeButton(R.string.cancel, null)
            }
            return@setOnItemLongClickListener true
        }
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllWatchHistories()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    historyAdapter.submitList(it)
                }
        }
    }

    override fun MainActivity.setupToolbar() {
        val toolbar = this@WatchHistoryFragment.binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setSubtitle(R.string.watch_history)
        this@WatchHistoryFragment.addMenu(
            R.menu.menu_watch_history_toolbar,
            viewLifecycleOwner
        ) { item ->
            when (item.itemId) {
                R.id.tb_delete -> {
                    requireContext().showAlertDialog {
                        setTitle(R.string.sure_to_delete)
                        setMessage(R.string.sure_to_delete_all_histories)
                        setPositiveButton(R.string.sure) { _, _ ->
                            viewModel.deleteAllWatchHistories()
                            historyAdapter.submitList(null)
                        }
                        setNegativeButton(R.string.no, null)
                    }
                    return@addMenu true
                }

                R.id.tb_help -> {
                    requireContext().showAlertDialog {
                        setTitle(R.string.attention)
                        setMessage(R.string.long_press_to_delete_all_histories)
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