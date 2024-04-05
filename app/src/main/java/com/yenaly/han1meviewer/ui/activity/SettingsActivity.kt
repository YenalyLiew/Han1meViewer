package com.yenaly.han1meviewer.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivitySettingsBinding
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.SystemStatusUtil

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 13:40
 */
class SettingsActivity : YenalyActivity<ActivitySettingsBinding, SettingsViewModel>() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    val currentFragment get() = navHostFragment.childFragmentManager.primaryNavigationFragment

    override fun setUiStyle() {
        SystemStatusUtil.fullScreen(window, true)
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_settings) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (!navController.navigateUp()) {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}