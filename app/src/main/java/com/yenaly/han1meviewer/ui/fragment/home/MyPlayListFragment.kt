package com.yenaly.han1meviewer.ui.fragment.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentPageListBinding
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:43
 */
class MyPlayListFragment : YenalyFragment<FragmentPageListBinding, MyListViewModel>() {
    override fun initData(savedInstanceState: Bundle?) {
        (activity as? MainActivity)?.setToolbarSubtitle(getString(R.string.play_list))
        setHasOptionsMenu(true)

    }

    override fun liveDataObserve() {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}