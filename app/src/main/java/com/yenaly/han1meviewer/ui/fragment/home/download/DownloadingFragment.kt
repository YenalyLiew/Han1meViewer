package com.yenaly.han1meviewer.ui.fragment.home.download

import android.os.Bundle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentListOnlyBinding
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.adapter.HanimeDownloadingRvAdapter
import com.yenaly.han1meviewer.ui.viewmodel.DownloadViewModel
import com.yenaly.han1meviewer.util.setStateViewLayout
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * 正在下载的影片
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/01 001 17:45
 */
class DownloadingFragment : YenalyFragment<FragmentListOnlyBinding, DownloadViewModel>(),
    StateLayoutMixin {

    private val adapter by unsafeLazy { HanimeDownloadingRvAdapter(this) }

    override fun initData(savedInstanceState: Bundle?) {
        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter
        adapter.setStateViewLayout(R.layout.layout_empty_view)
        binding.rvList.itemAnimator?.changeDuration = 0
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllDownloadingHanime().flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    adapter.submitList(it)
                }
        }
    }
}