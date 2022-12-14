package com.yenaly.han1meviewer.ui.fragment.home.download

import android.os.Bundle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentListOnlyBinding
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.adapter.HanimeDownloadedRvAdapter
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
class DownloadedFragment : YenalyFragment<FragmentListOnlyBinding, DownloadViewModel>() {

    private val adapter by unsafeLazy { HanimeDownloadedRvAdapter(this) }

    override fun initData(savedInstanceState: Bundle?) {

        clearMenu()

        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter
        adapter.setDiffCallback(HanimeDownloadedRvAdapter.COMPARATOR)

        (activity as? MainActivity)?.setToolbarSubtitle(getString(R.string.downloaded))
    }

    override fun liveDataObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllDownloadedHanime().flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    adapter.setDiffNewData(it)
                    if (it.isEmpty()) {
                        adapter.setEmptyView(R.layout.layout_empty_view)
                    }
                }
        }
    }
}