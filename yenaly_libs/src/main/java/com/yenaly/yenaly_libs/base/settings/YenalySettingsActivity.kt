package com.yenaly.yenaly_libs.base.settings

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.yenaly.yenaly_libs.R
import com.yenaly.yenaly_libs.base.frame.FrameActivity
import com.yenaly.yenaly_libs.databinding.YenalySettingsDataBinding

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/17 017 17:13
 * @Description : Description...
 */
abstract class YenalySettingsActivity : FrameActivity() {

    lateinit var binding: YenalySettingsDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.yenaly_activity_settings)
        binding.lifecycleOwner = this
        supportActionBar?.hide()
        setSupportActionBar(binding.settingsToolbar)
        binding.settingsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container_view, initFragmentContainer())
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }

    /**
     * 初始化设置的Fragment
     */
    abstract fun initFragmentContainer(): YenalySettingsFragment
}