package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentHKeyframesBinding
import com.yenaly.han1meviewer.logic.entity.HKeyframeType
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.adapter.SharedHKeyframesRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.LinearSmoothToStartScroller
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.han1meviewer.util.setStateViewLayout
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/18 018 17:38
 */
class SharedHKeyframesFragment : YenalyFragment<FragmentHKeyframesBinding>(),
    IToolbarFragment<SettingsActivity>, StateLayoutMixin {

    val viewModel by activityViewModels<SettingsViewModel>()

    private val adapter by unsafeLazy { SharedHKeyframesRvAdapter() }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHKeyframesBinding {
        return FragmentHKeyframesBinding.inflate(inflater, container, false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(context)
        val smoothScroller = LinearSmoothToStartScroller(context)
        binding.rvKeyframe.layoutManager = layoutManager
        binding.rvKeyframe.adapter = adapter
        binding.btnUp.setOnClickListener {
            var firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            if (adapter.getItemViewType(firstVisibleItemPosition) == HKeyframeType.HEADER) {
                firstVisibleItemPosition--
            }
            for (i in firstVisibleItemPosition downTo 0) {
                if (adapter.getItemViewType(i) == HKeyframeType.HEADER) {
                    smoothScroller.targetPosition = i
                    layoutManager.startSmoothScroll(smoothScroller)
                    break
                }
            }
        }
        binding.btnDown.setOnClickListener {
            var firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            if (adapter.getItemViewType(firstVisibleItemPosition) == HKeyframeType.HEADER) {
                firstVisibleItemPosition++
            }
            for (i in firstVisibleItemPosition..<adapter.itemCount) {
                if (adapter.getItemViewType(i) == HKeyframeType.HEADER) {
                    smoothScroller.targetPosition = i
                    layoutManager.startSmoothScroll(smoothScroller)
                    break
                }
            }
        }
        binding.rvKeyframe.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                binding.btnUp.isEnabled = recyclerView.canScrollVertically(-1)
                binding.btnDown.isEnabled = recyclerView.canScrollVertically(1)
            }
        })
        adapter.setStateViewLayout(R.layout.layout_empty_view, getString(R.string.here_is_empty))
    }

    override fun bindDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAllSharedHKeyframes().collect {
                adapter.submitList(it)
            }
        }
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.shared_h_keyframe_manage)
    }
}