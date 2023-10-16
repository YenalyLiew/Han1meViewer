package com.yenaly.han1meviewer.ui.fragment.home.download

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentListOnlyBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeDownloadedRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.DownloadViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * 已下载影片
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/01 001 17:45
 */
class DownloadedFragment : YenalyFragment<FragmentListOnlyBinding, DownloadViewModel>(),
    IToolbarFragment<MainActivity> {

    private val adapter by unsafeLazy { HanimeDownloadedRvAdapter(this) }

    override fun initData(savedInstanceState: Bundle?) {
        (activity as MainActivity).setupToolbar()
        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter
        adapter.setDiffCallback(HanimeDownloadedRvAdapter.COMPARATOR)

        loadAllSortedDownloadedHanime()
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloaded.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    adapter.setDiffNewData(it)
                    if (it.isEmpty()) {
                        adapter.setEmptyView(R.layout.layout_empty_view)
                    }
                }
        }
    }

    override fun MainActivity.setupToolbar() {
        val fv = this@DownloadedFragment.viewModel
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_download_toolbar, menu)
                menu.findItem(fv.currentSortOptionId).isChecked = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                fv.currentSortOptionId = menuItem.itemId
                menuItem.isChecked = true
                return loadAllSortedDownloadedHanime()
            }
        })
    }

    // #issue-18: 添加下载区排序
    private fun loadAllSortedDownloadedHanime(): Boolean = when (viewModel.currentSortOptionId) {
        R.id.sm_sort_by_alphabet_ascending -> {
            viewModel.loadAllDownloadedHanime(
                sortedBy = HanimeDownloadEntity.SortedBy.TITLE,
                ascending = true
            )
            true
        }

        R.id.sm_sort_by_alphabet_descending -> {
            viewModel.loadAllDownloadedHanime(
                sortedBy = HanimeDownloadEntity.SortedBy.TITLE,
                ascending = false
            )
            true
        }

        R.id.sm_sort_by_date_ascending -> {
            viewModel.loadAllDownloadedHanime(
                sortedBy = HanimeDownloadEntity.SortedBy.ID,
                ascending = true
            )
            true
        }

        R.id.sm_sort_by_date_descending -> {
            viewModel.loadAllDownloadedHanime(
                sortedBy = HanimeDownloadEntity.SortedBy.ID,
                ascending = false
            )
            true
        }

        else -> false
    }
}