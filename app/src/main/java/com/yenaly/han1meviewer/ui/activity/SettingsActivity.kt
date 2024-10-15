package com.yenaly.han1meviewer.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivitySettingsBinding
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.han1meviewer.util.logScreenViewEvent
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.intentExtra

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 13:40
 */
class SettingsActivity : YenalyActivity<ActivitySettingsBinding>() {

    companion object {
        const val H_KEYFRAME_SETTINGS = "h_keyframe"
    }

    val viewModel by viewModels<SettingsViewModel>()
    private val shouldNavToHKeyframeSettings by intentExtra(H_KEYFRAME_SETTINGS, false)

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    val currentFragment get() = navHostFragment.childFragmentManager.primaryNavigationFragment

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivitySettingsBinding =
        ActivitySettingsBinding.inflate(layoutInflater)

    override val onFragmentResumedListener: (Fragment) -> Unit = { fragment ->
        logScreenViewEvent(fragment)
    }

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.fade_in,
                R.anim.fade_out
            )
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_settings) as NavHostFragment
        navController = navHostFragment.navController
        if (shouldNavToHKeyframeSettings) {
            navController.navigate(R.id.action_homeSettingsFragment_pop_to_hKeyframeSettingsFragment)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.fcvSettings) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = navBar.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (!navController.popBackStack()) {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}