package com.yenaly.han1meviewer.ui.fragment.settings

import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.hanimeVideoLocalFolder
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.yenaly_libs.ActivitiesManager
import com.yenaly.yenaly_libs.base.preference.LongClickablePreference
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import rikka.preference.SimpleMenuPreference

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment() {

    private val videoLanguageListPreference by safePreference<SimpleMenuPreference>("video_language")
    private val updatePreference by safePreference<Preference>("update")
    private val aboutPreference by safePreference<Preference>("about")
    private val downloadPath by safePreference<LongClickablePreference>("download_path")

    override fun initPreferencesResource() = R.xml.settings_home

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguageListPreference.setOnPreferenceChangeListener { _, newValue ->
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
        aboutPreference.setOnPreferenceClickListener {
            startActivity<AboutActivity>()
            return@setOnPreferenceClickListener true
        }
        downloadPath.apply {
            val path = hanimeVideoLocalFolder?.path
            summary = path
            setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("不允許更改哦")
                    .setMessage(
                        "Android 權限收太緊了，將就著用吧！\n" +
                                "詳細位置：${path}\n" +
                                "長按選項可以複製哦！"
                    )
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnPreferenceClickListener true
            }
            setOnPreferenceLongClickListener {
                path.copyToClipboard()
                showShortToast("已將完整路徑複製於剪貼簿")
                return@setOnPreferenceLongClickListener true
            }
        }
    }
}