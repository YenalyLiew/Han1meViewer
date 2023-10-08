package com.yenaly.han1meviewer.ui.fragment.settings

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.HProxySelector
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.network.HanimeNetwork
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.activity.AboutActivity
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
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
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch
import rikka.preference.SimpleMenuPreference
import kotlin.concurrent.thread

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 14:25
 */
class HomeSettingsFragment : YenalySettingsFragment(R.xml.settings_home),
    IToolbarFragment<SettingsActivity> {

    private val viewModel by activityViewModels<SettingsViewModel>()

    companion object {
        const val VIDEO_LANGUAGE = "video_language"
        const val PLAYER_SETTINGS = "player_settings"
        const val UPDATE = "update"
        const val ABOUT = "about"
        const val DOWNLOAD_PATH = "download_path"
        const val CLEAR_CACHE = "clear_cache"
        const val PROXY = "proxy"
        const val PROXY_TYPE = "proxy_type"
        const val PROXY_IP = "proxy_ip"
        const val PROXY_PORT = "proxy_port"
    }

    private val videoLanguage
            by safePreference<SimpleMenuPreference>(VIDEO_LANGUAGE)
    private val playerSettings
            by safePreference<Preference>(PLAYER_SETTINGS)
    private val update
            by safePreference<Preference>(UPDATE)
    private val about
            by safePreference<Preference>(ABOUT)
    private val downloadPath
            by safePreference<LongClickablePreference>(DOWNLOAD_PATH)
    private val clearCache
            by safePreference<Preference>(CLEAR_CACHE)
    private val proxy
            by safePreference<Preference>(PROXY)

    private val proxyDialog by unsafeLazy {
        ProxyDialog(proxy, R.layout.dialog_proxy)
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        videoLanguage.apply {

            // 從 xml 轉移至此
            entries = arrayOf("繁體中文", "簡體中文")
            entryValues = arrayOf("zh-CHT", "zh-CHS")
            // 不能直接用 defaultValue 设置，没效果
            if (value == null) setValueIndex(0)

            setOnPreferenceChangeListener { _, newValue ->
                if (newValue != preferenceSp.getString(VIDEO_LANGUAGE, "zh-CHT")) {
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
        }
        playerSettings.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_homeSettingsFragment_to_playerSettingsFragment)
            return@setOnPreferenceClickListener true
        }
        about.apply {
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
                        "詳細位置：${path}\n" + "長按選項可以複製哦！"
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
        proxy.apply {
            summary = generateProxySummary(
                preferenceSp.getInt(PROXY_TYPE, HProxySelector.TYPE_SYSTEM),
                preferenceSp.getString(PROXY_IP, EMPTY_STRING).orEmpty(),
                preferenceSp.getInt(PROXY_PORT, -1)
            )
            setOnPreferenceClickListener {
                proxyDialog.show()
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
                            update.setSummary(R.string.check_update_failed)
                            update.setOnPreferenceClickListener {
                                viewModel.getLatestVersion()
                                return@setOnPreferenceClickListener true
                            }
                        }

                        is WebsiteState.Loading -> {
                            update.setSummary(R.string.checking_update)
                            update.onPreferenceClickListener = null
                        }

                        is WebsiteState.Success -> {
                            if (checkNeedUpdate(state.info.tagName)) {
                                update.summary =
                                    getString(R.string.check_update_success, state.info.tagName)
                                update.setOnPreferenceClickListener {
                                    browse(state.info.assets.first().browserDownloadURL)
                                    return@setOnPreferenceClickListener true
                                }
                            } else {
                                update.setSummary(R.string.already_latest_update)
                                update.onPreferenceClickListener = null
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

    private fun generateProxySummary(type: Int, ip: String, port: Int): CharSequence {
        return when (type) {
            HProxySelector.TYPE_DIRECT -> getString(R.string.direct)
            HProxySelector.TYPE_SYSTEM -> getString(R.string.system_proxy)
            HProxySelector.TYPE_HTTP -> getString(R.string.http_proxy, ip, port)
            HProxySelector.TYPE_SOCKS -> getString(R.string.socks_proxy, ip, port)
            else -> getString(R.string.direct)
        }
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.settings)
    }

    inner class ProxyDialog(proxyPref: Preference, @LayoutRes layoutRes: Int) {

        private val dialog: AlertDialog

        private val cgTypes: ChipGroup
        private val etIp: TextInputEditText
        private val etPort: TextInputEditText

        init {
            val view = View.inflate(context, layoutRes, null)
            cgTypes = view.findViewById(R.id.cg_types)
            etIp = view.findViewById(R.id.et_ip)
            etPort = view.findViewById(R.id.et_port)
            initView()
            dialog = MaterialAlertDialogBuilder(proxyPref.context)
                .setView(view)
                .setTitle(R.string.proxy)
                .setPositiveButton(R.string.confirm, null) // Set to null. We override the onclick.
                .setNegativeButton(R.string.cancel, null)
                .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val ip = etIp.text?.toString().orEmpty()
                    val port = etPort.text?.toString()?.toIntOrNull() ?: -1
                    val isValid = checkValid(ip, port)
                    if (isValid) {
                        val proxyType = proxyType
                        preferenceSp.edit(commit = true) {
                            putInt(PROXY_TYPE, proxyType)
                            putString(PROXY_IP, ip)
                            putInt(PROXY_PORT, port)
                        }
                        // 重建相关联的所有网络请求
                        HanimeNetwork.rebuildNetwork()
                        proxyPref.summary = generateProxySummary(proxyType, ip, port)
                        dialog.dismiss()
                    } else {
                        showShortToast("Invalid IP(v4) or Port(0..65535)")
                    }
                }
            }
        }

        private fun initView() {
            when (preferenceSp.getInt(PROXY_TYPE, HProxySelector.TYPE_SYSTEM)) {
                HProxySelector.TYPE_DIRECT -> cgTypes.check(R.id.chip_direct)
                HProxySelector.TYPE_SYSTEM -> cgTypes.check(R.id.chip_system_proxy)
                HProxySelector.TYPE_HTTP -> cgTypes.check(R.id.chip_http)
                HProxySelector.TYPE_SOCKS -> cgTypes.check(R.id.chip_socks)
            }
            val prefIp = preferenceSp.getString(PROXY_IP, EMPTY_STRING)
            val prefPort = preferenceSp.getInt(PROXY_PORT, -1)
            if (!prefIp.isNullOrBlank() && prefPort != -1) {
                etIp.setText(prefIp)
                etPort.setText(prefPort.toString())
            }
            enableView(cgTypes.checkedChipId)
            cgTypes.setOnCheckedStateChangeListener { _, checkedIds ->
                enableView(checkedIds.first())
            }
        }

        private val proxyType: Int
            get() = when (cgTypes.checkedChipId) {
                R.id.chip_direct -> HProxySelector.TYPE_DIRECT
                R.id.chip_system_proxy -> HProxySelector.TYPE_SYSTEM
                R.id.chip_http -> HProxySelector.TYPE_HTTP
                R.id.chip_socks -> HProxySelector.TYPE_SOCKS
                else -> HProxySelector.TYPE_DIRECT
            }

        private fun checkValid(ip: String, port: Int): Boolean {
            return when (proxyType) {
                HProxySelector.TYPE_DIRECT, HProxySelector.TYPE_SYSTEM -> true
                HProxySelector.TYPE_HTTP, HProxySelector.TYPE_SOCKS -> {
                    HProxySelector.validateIp(ip) && HProxySelector.validatePort(port)
                }

                else -> false
            }
        }

        private fun enableView(@IdRes checkedId: Int) {
            when (checkedId) {
                R.id.chip_direct -> {
                    etIp.isEnabled = false
                    etPort.isEnabled = false
                    etIp.text = null
                    etPort.text = null
                }

                R.id.chip_system_proxy -> {
                    etIp.isEnabled = false
                    etPort.isEnabled = false
                    etIp.text = null
                    etPort.text = null
                }

                R.id.chip_http -> {
                    etIp.isEnabled = true
                    etPort.isEnabled = true
                }

                R.id.chip_socks -> {
                    etIp.isEnabled = true
                    etPort.isEnabled = true
                }
            }
        }

        fun show() {
            initView()
            dialog.show()
        }
    }
}