package com.yenaly.han1meviewer.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.text.parseAsHtml
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityDownloadBinding
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.view.attach
import com.yenaly.yenaly_libs.utils.view.setUpFragmentStateAdapter

class DownloadActivity : YenalyActivity<ActivityDownloadBinding>() {

    companion object {
        const val TAG = "HoppinByte"

        private const val HB = """<span style="color: #FF0000;"><b>H</b></span>oppin<b>Byte</b>"""
        val hbSpannedTitle = HB.parseAsHtml()
    }

    private val tabNameArray = intArrayOf(R.string.downloading, R.string.downloaded)

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityDownloadBinding {
        return ActivityDownloadBinding.inflate(layoutInflater)
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.let {
            it.title = hbSpannedTitle
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }

        binding.viewPager.setUpFragmentStateAdapter(this) {
            addFragment { DownloadingFragment() }
            addFragment { DownloadedFragment() }
        }

        binding.tabLayout.attach(binding.viewPager) { tab, position ->
            tab.setText(tabNameArray[position])
        }
    }


    override fun bindDataObservers() {

    }
}