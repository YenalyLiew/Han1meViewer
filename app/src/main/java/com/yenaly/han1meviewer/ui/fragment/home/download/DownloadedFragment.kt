package com.yenaly.han1meviewer.ui.fragment.home.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentListOnlyBinding
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeDownloadedRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.viewmodel.DownloadViewModel
import com.yenaly.han1meviewer.util.setStateViewLayout
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
class DownloadedFragment : YenalyFragment<FragmentListOnlyBinding>(),
    IToolbarFragment<MainActivity>, StateLayoutMixin {

    val viewModel by activityViewModels<DownloadViewModel>()

    private val adapter by unsafeLazy { HanimeDownloadedRvAdapter(this) }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListOnlyBinding {
        return FragmentListOnlyBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        (activity as MainActivity).setupToolbar()

        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter
        ViewCompat.setOnApplyWindowInsetsListener(binding.rvList) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = navBar.bottom)
            WindowInsetsCompat.CONSUMED
        }
        adapter.setStateViewLayout(R.layout.layout_empty_view)
        loadAllSortedDownloadedHanime()
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloaded.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    adapter.submitList(it)
                }
        }
    }

    override fun MainActivity.setupToolbar() {
        val fv = this@DownloadedFragment.viewModel
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_downloaded_toolbar, menu)
                menu.findItem(fv.currentSortOptionId).isChecked = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                fv.currentSortOptionId = menuItem.itemId
                menuItem.isChecked = true
                return loadAllSortedDownloadedHanime()
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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