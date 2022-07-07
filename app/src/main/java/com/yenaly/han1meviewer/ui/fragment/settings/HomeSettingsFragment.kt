package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.yenaly_libs.ActivitiesManager
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.startActivity
import rikka.preference.SimpleMenuPreference

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment() {

    private var videoLanguageListPreference: SimpleMenuPreference? = null
    private var updatePreference: Preference? = null
    private var aboutPreference: Preference? = null

    override fun initPreferencesResource() = R.xml.settings_home

    override fun initPreferencesVariable() {
        videoLanguageListPreference = findPreference("video_language")
        updatePreference = findPreference("update")
        aboutPreference = findPreference("about")
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguageListPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue != preferenceSp.getString("video_language", "zh-CHT")) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("修改影片語言需要重新程式")
                    .setPositiveButton("確認") { _, _ ->
                        ActivitiesManager.restartAppWithoutKillingProcess()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
            return@setOnPreferenceChangeListener true
        }
        aboutPreference?.setOnPreferenceClickListener {
            startActivity<AboutActivity>()
            return@setOnPreferenceClickListener true
        }
    }
}