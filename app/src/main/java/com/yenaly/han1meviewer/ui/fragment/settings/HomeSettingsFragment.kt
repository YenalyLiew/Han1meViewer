package com.yenaly.han1meviewer.ui.fragment.settings

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.preference.Preference
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.han1meviewer.util.checkNeedUpdate
import com.yenaly.han1meviewer.util.hanimeVideoLocalFolder
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.ActivitiesManager
import com.yenaly.yenaly_libs.base.preference.LongClickablePreference
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.appLocalVersionName
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.folderSize
import com.yenaly.yenaly_libs.utils.formatFileSize
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.coroutines.launch
import rikka.preference.SimpleMenuPreference
import kotlin.concurrent.thread

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment(R.xml.settings_home) {

    private val viewModel by activityViewModels<SettingsViewModel>()

    private val videoLanguageListPreference by safePreference<SimpleMenuPreference>("video_language")
    private val updatePreference by safePreference<Preference>("update")
    private val aboutPreference by safePreference<Preference>("about")
    private val downloadPath by safePreference<LongClickablePreference>("download_path")
    private val clearCache by safePreference<Preference>("clear_cache")

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguageListPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue != preferenceSp.getString("video_language", "zh-CHT")) {
                requireContext().showAlertDialog {
                    setTitle("注意！")
                    setMessage("修改影片語言需要重新程式")
                    setPositiveButton("確認") { _, _ ->
                        ActivitiesManager.restart(killProcess = false)
                    }
                    setNegativeButton("取消", null)
                }
            }
            return@setOnPreferenceChangeListener true
        }
        aboutPreference.apply {
            title = buildString {
                append(getString(R.string.about))
                append(" ")
                append(getString(R.string.hanime_app_name))
            }
            summary = getString(R.string.current_version, "v${appLocalVersionName}")
            setOnPreferenceClickListener {
                startActivity<AboutActivity>()
                return@setOnPreferenceClickListener true
            }
        }
        downloadPath.apply {
            val path = hanimeVideoLocalFolder?.path
            summary = path
            setOnPreferenceClickListener {
                requireContext().showAlertDialog {
                    setTitle("不允許更改哦")
                    setMessage(
                        "Android 權限收太緊了，將就著用吧！\n" +
                                "詳細位置：${path}\n" +
                                "長按選項可以複製哦！"
                    )
                    setPositiveButton("OK", null)
                }
                return@setOnPreferenceClickListener true
            }
            setOnPreferenceLongClickListener {
                path.copyToClipboard()
                showShortToast(R.string.copy_to_clipboard)
                return@setOnPreferenceLongClickListener true
            }
        }
        clearCache.apply {
            val cacheDir = context.cacheDir
            var folderSize = cacheDir?.folderSize ?: 0L
            summary = generateClearCacheSummary(folderSize)
            // todo: strings.xml
            setOnPreferenceClickListener {
                if (folderSize != 0L) {
                    context.showAlertDialog {
                        setTitle("請再次確認一遍")
                        setMessage("確定要清除快取嗎？")
                        setPositiveButton(R.string.confirm) { _, _ ->
                            thread {
                                if (cacheDir?.deleteRecursively() == true) {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast("清除成功")
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                } else {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast("清除發生意外")
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                }
                            }
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                } else showShortToast("當前快取為空，無需清理哦")
                return@setOnPreferenceClickListener true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFlow()
    }

    private fun initFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            whenStarted {
                viewModel.versionFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            updatePreference.setSummary(R.string.check_update_failed)
                            updatePreference.setOnPreferenceClickListener {
                                viewModel.getLatestVersion()
                                return@setOnPreferenceClickListener true
                            }
                        }
                        is WebsiteState.Loading -> {
                            updatePreference.setSummary(R.string.checking_update)
                            updatePreference.onPreferenceClickListener = null
                        }
                        is WebsiteState.Success -> {
                            if (checkNeedUpdate(state.info.tagName)) {
                                updatePreference.summary =
                                    getString(R.string.check_update_success, state.info.tagName)
                                updatePreference.setOnPreferenceClickListener {
                                    browse(state.info.assets.first().browserDownloadURL)
                                    return@setOnPreferenceClickListener true
                                }
                            } else {
                                updatePreference.setSummary(R.string.already_latest_update)
                                updatePreference.onPreferenceClickListener = null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateClearCacheSummary(size: Long): CharSequence {
        return spannable {
            size.formatFileSize().span {
                style(Typeface.BOLD)
            }
            " ".text()
            getString(R.string.cache_occupy).text()
        }
    }
}