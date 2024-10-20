package com.yenaly.han1meviewer.ui.fragment.settings

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.BuildConfig
import com.yenaly.han1meviewer.HA1_GITHUB_FORUM_URL
import com.yenaly.han1meviewer.HA1_GITHUB_ISSUE_URL
import com.yenaly.han1meviewer.HA1_GITHUB_RELEASES_URL
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.pref.HPrivacyPreference
import com.yenaly.han1meviewer.ui.view.pref.MaterialDialogPreference
import com.yenaly.han1meviewer.ui.viewmodel.AppViewModel
import com.yenaly.han1meviewer.util.hanimeVideoLocalFolder
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.util.showUpdateDialog
import com.yenaly.han1meviewer.util.showWithBlurEffect
import com.yenaly.yenaly_libs.ActivityManager
import com.yenaly.yenaly_libs.base.preference.LongClickablePreference
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.browse
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.folderSize
import com.yenaly.yenaly_libs.utils.formatFileSize
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.concurrent.thread

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment(R.xml.settings_home),
    IToolbarFragment<SettingsActivity> {

    companion object {
        const val VIDEO_LANGUAGE = "video_language"
        const val PLAYER_SETTINGS = "player_settings"
        const val H_KEYFRAME_SETTINGS = "h_keyframe_settings"
        const val UPDATE = "update"
        const val ABOUT = "about"
        const val DOWNLOAD_PATH = "download_path"
        const val CLEAR_CACHE = "clear_cache"
        const val SUBMIT_BUG = "submit_bug"
        const val FORUM = "forum"
        const val NETWORK_SETTINGS = "network_settings"
        const val APPLY_DEEP_LINKS = "apply_deep_links"

        const val LAST_UPDATE_POPUP_TIME = "last_update_popup_time"
        const val UPDATE_POPUP_INTERVAL_DAYS = "update_popup_interval_days"
        const val USE_CI_UPDATE_CHANNEL = "use_ci_update_channel"

        const val USE_ANALYTICS = "use_analytics"
    }

    private val videoLanguage
            by safePreference<MaterialDialogPreference>(VIDEO_LANGUAGE)
    private val playerSettings
            by safePreference<Preference>(PLAYER_SETTINGS)
    private val hKeyframeSettings
            by safePreference<Preference>(H_KEYFRAME_SETTINGS)
    private val update
            by safePreference<Preference>(UPDATE)
    private val useCIUpdateChannel
            by safePreference<SwitchPreferenceCompat>(USE_CI_UPDATE_CHANNEL)
    private val updatePopupIntervalDays
            by safePreference<SeekBarPreference>(UPDATE_POPUP_INTERVAL_DAYS)
    private val about
            by safePreference<Preference>(ABOUT)
    private val downloadPath
            by safePreference<LongClickablePreference>(DOWNLOAD_PATH)
    private val clearCache
            by safePreference<Preference>(CLEAR_CACHE)
    private val submitBug
            by safePreference<Preference>(SUBMIT_BUG)
    private val forum
            by safePreference<Preference>(FORUM)
    private val networkSettings
            by safePreference<Preference>(NETWORK_SETTINGS)
    private val applyDeepLinks
            by safePreference<Preference>(APPLY_DEEP_LINKS)
    private val useAnalytics
            by safePreference<HPrivacyPreference>(USE_ANALYTICS)

    private var checkUpdateTimes = 0

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguage.apply {

            // 從 xml 轉移至此
            entries = arrayOf(
                getString(R.string.traditional_chinese),
                getString(R.string.simplified_chinese)
            )
            entryValues = arrayOf("zh-CHT", "zh-CHS")
            // 不能直接用 defaultValue 设置，没效果
            if (value == null) setValueIndex(0)

            setOnPreferenceChangeListener { _, newValue ->
                if (newValue != Preferences.videoLanguage) {
                    requireContext().showAlertDialog {
                        setCancelable(false)
                        setTitle(R.string.attention)
                        setMessage(
                            getString(
                                R.string.restart_or_not_working,
                                getString(R.string.video_language)
                            )
                        )
                        setPositiveButton(R.string.confirm) { _, _ ->
                            ActivityManager.restart(killProcess = true)
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                }
                return@setOnPreferenceChangeListener true
            }
        }
        playerSettings.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_homeSettingsFragment_to_playerSettingsFragment)
            return@setOnPreferenceClickListener true
        }
        hKeyframeSettings.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_homeSettingsFragment_to_hKeyframeSettingsFragment)
            return@setOnPreferenceClickListener true
        }
        about.apply {
            title = buildString {
                append(getString(R.string.about))
                append(" ")
                append(getString(R.string.hanime_app_name))
            }
            summary = getString(R.string.current_version, "v${BuildConfig.VERSION_NAME}")
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
                    setTitle(R.string.not_allow_to_change)
                    setMessage(
                        getString(R.string.detailed_path_s, path) + "\n"
                                + getString(R.string.long_press_pref_to_copy)
                    )
                    setPositiveButton(R.string.ok, null)
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
            setOnPreferenceClickListener {
                if (folderSize != 0L) {
                    context.showAlertDialog {
                        setTitle(R.string.sure_to_clear)
                        setMessage(R.string.sure_to_clear_cache)
                        setPositiveButton(R.string.confirm) { _, _ ->
                            thread {
                                if (cacheDir?.deleteRecursively() == true) {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast(R.string.clear_success)
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                } else {
                                    folderSize = cacheDir.folderSize
                                    activity?.runOnUiThread {
                                        showShortToast(R.string.clear_failed)
                                        summary = generateClearCacheSummary(folderSize)
                                    }
                                }
                            }
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                } else showShortToast(R.string.cache_empty)
                return@setOnPreferenceClickListener true
            }
        }
        submitBug.apply {
            setOnPreferenceClickListener {
                browse(HA1_GITHUB_ISSUE_URL)
                return@setOnPreferenceClickListener true
            }
        }
        forum.apply {
            setOnPreferenceClickListener {
                browse(HA1_GITHUB_FORUM_URL)
                return@setOnPreferenceClickListener true
            }
        }
        networkSettings.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_homeSettingsFragment_to_networkSettingsFragment)
                return@setOnPreferenceClickListener true
            }
        }
        applyDeepLinks.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                isVisible = true
                setOnPreferenceClickListener {
                    showApplyDeepLinksDialog(it.context)
                    return@setOnPreferenceClickListener true
                }
            } else {
                isVisible = false
            }
        }
        useCIUpdateChannel.apply {
            setOnPreferenceChangeListener { _, _ ->
                AppViewModel.getLatestVersion()
                return@setOnPreferenceChangeListener true
            }
        }
        updatePopupIntervalDays.apply {
            summary = Preferences.updatePopupIntervalDays.toIntervalDaysPrettyString()
            setOnPreferenceChangeListener { _, newValue ->
                summary = (newValue as Int).toIntervalDaysPrettyString()
                return@setOnPreferenceChangeListener true
            }
        }
        useAnalytics.apply {
            setOnPreferenceChangeListener { _, newValue ->
                Firebase.analytics.setAnalyticsCollectionEnabled(newValue as Boolean)
                return@setOnPreferenceChangeListener true
            }
            setOnPreferenceLongClickListener {
                privacyDialog.showWithBlurEffect()
                return@setOnPreferenceLongClickListener true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFlow()
    }

    private fun initFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppViewModel.versionFlow.collect { state ->
                    when (state) {
                        is WebsiteState.Error -> {
                            checkUpdateTimes++
                            update.setSummary(R.string.check_update_failed)
                            update.setOnPreferenceClickListener {
                                if (checkUpdateTimes > 2) {
                                    showUpdateFailedDialog(it.context)
                                } else AppViewModel.getLatestVersion()
                                return@setOnPreferenceClickListener true
                            }
                        }

                        is WebsiteState.Loading -> {
                            update.setSummary(R.string.checking_update)
                            update.onPreferenceClickListener = null
                        }

                        is WebsiteState.Success -> {
                            if (state.info == null) {
                                update.setSummary(R.string.already_latest_update)
                                update.onPreferenceClickListener = null
                            } else {
                                update.summary =
                                    getString(R.string.check_update_success, state.info.version)
                                update.setOnPreferenceClickListener {
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        it.context.showUpdateDialog(state.info)
                                    }
                                    return@setOnPreferenceClickListener true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // #issue-124: Support deep links.
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showApplyDeepLinksDialog(context: Context) {
        context.showAlertDialog {
            setTitle(R.string.apply_deep_links)
            setView(R.layout.dialog_apply_deep_links)
            setPositiveButton(R.string.go_to_settings) { _, _ ->
                // #issue-197: 有些手机不支持直接从应用里跳转到深层链接界面
                // 这个权限是一个系统级权限，所以没办法，不支持的手机只能自己找地方开了。
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = "package:${context.packageName}".toUri()
                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    }
                    requireActivity().startActivity(intent)
                } catch (e: Exception) {
                    // 竟然还有手机不支持打开的
                    showShortToast(R.string.action_app_open_by_default_settings_not_support)
                    e.printStackTrace()
                }
            }
            setNegativeButton(R.string.cancel, null)
        }
    }

    private fun showUpdateFailedDialog(context: Context) {
        context.showAlertDialog {
            setTitle(R.string.do_not_check_update_again)
            setMessage(getString(R.string.update_failed_tips).trimIndent())
            setPositiveButton(R.string.take_me_to_download) { _, _ ->
                browse(HA1_GITHUB_RELEASES_URL)
            }
            setNegativeButton(R.string.cancel, null)
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

    private fun Int.toIntervalDaysPrettyString(): String {
        return when (this) {
            0 -> getString(R.string.at_any_time)
            else -> getString(R.string.which_days, this)
        } + "\n" + getString(
            R.string.last_update_popup_check_time,
            Instant.fromEpochSeconds(Preferences.lastUpdatePopupTime)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Formats.ISO)
        )
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.settings)
    }
}