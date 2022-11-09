package com.yenaly.han1meviewer.ui.fragment.home

import android.os.Bundle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentListOnlyBinding
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.WatchHistoryRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.showSnackBar
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 21:23
 */
class WatchHistoryFragment : YenalyFragment<FragmentListOnlyBinding, MainViewModel>() {

    private val historyAdapter by unsafeLazy { WatchHistoryRvAdapter() }

    override fun initData(savedInstanceState: Bundle?) {

        (activity as? MainActivity)?.setToolbarSubtitle(getString(R.string.watch_history))

        addMenu(R.menu.menu_watch_history_toolbar, viewLifecycleOwner) { item ->
            when (item.itemId) {
                R.id.tb_delete -> {
                    // todo: strings.xml
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("看這裏！")
                        .setMessage("是否將影片觀看歷史記錄全部刪除🤔")
                        .setPositiveButton("是的！") { _, _ ->
                            viewModel.deleteAllWatchHistories()
                        }
                        .setNegativeButton("算了！", null)
                        .show()
                    return@addMenu true
                }
                R.id.tb_help -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("使用注意！")
                        .setMessage("左劃可以刪除歷史記錄哦，右上角的刪除按鈕是負責刪除全部歷史記錄的！")
                        .setPositiveButton("OK", null)
                        .show()
                    return@addMenu true
                }
            }
            return@addMenu false
        }

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
        historyAdapter.setDiffCallback(WatchHistoryRvAdapter.COMPARATOR)
        historyAdapter.setEmptyView(R.layout.layout_empty_view)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

            // 上下滑動
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            // 左右滑動
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val data = historyAdapter.getItem(position)
                historyAdapter.remove(data)
                // todo: strings.xml
                showSnackBar("你正在刪除該歷史記錄", Snackbar.LENGTH_LONG) {
                    setAction("撤銷") {
                        historyAdapter.addData(position, data)
                    }
                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deleteWatchHistory(data)
                            }
                        }
                    })
                }
            }
        }).attachToRecyclerView(binding.rvList)
    }

    override fun liveDataObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllWatchHistories()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    if (it.isEmpty()) {
                        historyAdapter.setEmptyView(R.layout.layout_empty_view)
                    } else {
                        historyAdapter.setDiffNewData(it)
                    }
                }
        }
    }
}