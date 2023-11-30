package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentHKeyframesBinding
import com.yenaly.han1meviewer.ui.StateLayoutMixin
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.adapter.HKeyframesRvAdapter
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
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
class SharedHKeyframesFragment :
    YenalyFragment<FragmentHKeyframesBinding, SettingsViewModel>(),
    IToolbarFragment<SettingsActivity>, StateLayoutMixin {

    private val adapter by unsafeLazy { HKeyframesRvAdapter() }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun initData(savedInstanceState: Bundle?) {
        binding.rvKeyframe.layoutManager = LinearLayoutManager(context)
        binding.rvKeyframe.adapter = adapter
        adapter.setStateViewLayout(R.layout.layout_empty_view, buildString {
            appendLine(getString(R.string.here_is_empty))
            append("還沒有好心人共享，如果想貢獻非常歡迎！")
        })
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