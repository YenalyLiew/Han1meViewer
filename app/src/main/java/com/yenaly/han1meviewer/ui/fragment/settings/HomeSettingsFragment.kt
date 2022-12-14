package com.yenaly.han1meviewer.ui.fragment.settings

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.util.checkNeedUpdate
import com.yenaly.han1meviewer.util.hanimeVideoLocalFolder
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.han1meviewer.ui.viewmodel.SettingsViewModel
import com.yenaly.yenaly_libs.ActivitiesManager
import com.yenaly.yenaly_libs.base.preference.LongClickablePreference
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.*
import kotlinx.coroutines.launch
import rikka.preference.SimpleMenuPreference
import kotlin.concurrent.thread

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment() {

    private val viewModel by activityViewModels<SettingsViewModel>()

    private val videoLanguageListPreference by safePreference<SimpleMenuPreference>("video_language")
    private val updatePreference by safePreference<Preference>("update")
    private val aboutPreference by safePreference<Preference>("about")
    private val downloadPath by safePreference<LongClickablePreference>("download_path")
    private val clearCache by safePreference<Preference>("clear_cache")

    override fun initPreferencesResource() = R.xml.settings_home

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguageListPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue != preferenceSp.getString("video_language", "zh-CHT")) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("????????????????????????????????????")
                    .setPositiveButton("??????") { _, _ ->
                        ActivitiesManager.restartAppWithoutKillingProcess()
                    }
                    .setNegativeButton("??????", null)
                    .show()
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
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("??????????????????")
                    .setMessage(
                        "Android ???????????????????????????????????????\n" +
                                "???????????????${path}\n" +
                                "??????????????????????????????"
                    )
                    .setPositiveButton("OK", null)
                    .show()
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
                    MaterialAlertDialogBuilder(context)
                        .setTitle("??????????????????")
                        .setMessage("???????????????????????????")
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            thread {
                                if (cacheDir?.deleteRecursively() == true) {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast("????????????")
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                } else {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast("??????????????????")
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                }
                            }
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else showShortToast("????????????????????????????????????")
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