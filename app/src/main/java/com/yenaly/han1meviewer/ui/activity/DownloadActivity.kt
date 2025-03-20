package com.yenaly.han1meviewer.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.parseAsHtml
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityDownloadBinding
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadedFragment
import com.yenaly.han1meviewer.ui.fragment.home.download.DownloadingFragment
import com.yenaly.han1meviewer.ui.view.funcbar.Hanidokitem
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.showShortToast
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_in_from_bottom,
                R.anim.fade_out
            )
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.fade_in,
                R.anim.slide_out_to_bottom
            )
        }

        binding.viewPager.setUpFragmentStateAdapter(this) {
            addFragment { DownloadingFragment() }
            addFragment { DownloadedFragment() }
        }

        binding.tabLayout.attach(binding.viewPager) { tab, position ->
            tab.setText(tabNameArray[position])
        }

        binding.hanidock.hanidokitems = listOf(
            Hanidokitem.create {
                icon = R.drawable.ic_baseline_access_time_24
                text = R.string.title
                viewAction = View.OnClickListener {
                    showShortToast("test")
                }
            },
            Hanidokitem.create {
                icon = R.drawable.baseline_add_24
                text = R.string.add
                subitems = listOf(
                    Hanidokitem.create {
                        icon = R.drawable.ic_baseline_access_time_24
                        text = R.string.title
                    }
                )
            }
        )
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_to_bottom)
        }
    }


    override fun bindDataObservers() {

    }
}