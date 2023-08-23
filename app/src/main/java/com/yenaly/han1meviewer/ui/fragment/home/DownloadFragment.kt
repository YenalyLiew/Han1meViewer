package com.yenaly.han1meviewer.ui.fragment.home

import android.os.Bundle
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.FragmentTabViewPagerOnlyBinding
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.ui.viewmodel.DownloadViewModel
import com.yenaly.yenaly_libs.base.YenalyFragment
import com.yenaly.yenaly_libs.utils.view.attach
import com.yenaly.yenaly_libs.utils.view.setUpFragmentStateAdapter

/**
 * 下载影片总Fragment，暫時由[DownloadedFragment]全權托管
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/01 001 17:44
 */
class DownloadFragment : YenalyFragment<FragmentTabViewPagerOnlyBinding, DownloadViewModel>() {

    private val tabNameArray = intArrayOf(R.string.downloading, R.string.downloaded)

    override fun initData(savedInstanceState: Bundle?) {
        clearMenu()
        initViewPager()
    }

    private fun initViewPager() {

        binding.viewPager.setUpFragmentStateAdapter(this) {
            addFragment { DownloadingFragment() }
            addFragment { DownloadedFragment() }
        }

        binding.tabLayout.attach(binding.viewPager) { tab, position ->
            tab.setText(tabNameArray[position])
        }
    }
}