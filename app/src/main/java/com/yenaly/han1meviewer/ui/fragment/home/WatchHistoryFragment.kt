package com.yenaly.han1meviewer.ui.fragment.home

import android.os.Bundle
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
import com.yenaly.han1meviewer.util.notNull
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
class WatchHistoryFragment : YenalyFragment<FragmentPageListBinding, MainViewModel>(),
    IToolbarFragment<MainActivity>, StateLayoutMixin {

    private val historyAdapter by unsafeLazy { WatchHistoryRvAdapter() }

    override fun initData(savedInstanceState: Bundle?) {
        (activity as MainActivity).setupToolbar()

        binding.rvPageList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
        binding.srlPageList.finishRefreshWithNoMoreData()
        historyAdapter.setStateViewLayout(R.layout.layout_empty_view)
        historyAdapter.setOnItemLongClickListener { _, _, position ->
            val data = historyAdapter.getItem(position).notNull()
            requireContext().showAlertDialog {
                setTitle("刪除歷史記錄")
                setMessage(getString(R.string.sure_to_delete_s_video, data.title))
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
                    // todo: strings.xml
                    requireContext().showAlertDialog {
                        setTitle("看這裏！")
                        setMessage("是否將影片觀看歷史記錄全部刪除🤔")
                        setPositiveButton("是的！") { _, _ ->
                            viewModel.deleteAllWatchHistories()
                            historyAdapter.submitList(null)
                        }
                        setNegativeButton("算了！", null)
                    }
                    return@addMenu true
                }

                R.id.tb_help -> {
                    requireContext().showAlertDialog {
                        setTitle("使用注意！")
                        setMessage("長按可以刪除歷史記錄哦，右上角的刪除按鈕是負責刪除全部歷史記錄的！")
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