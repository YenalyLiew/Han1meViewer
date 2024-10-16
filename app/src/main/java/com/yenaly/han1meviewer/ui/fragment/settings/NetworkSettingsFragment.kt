package com.yenaly.han1meviewer.ui.fragment.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.HANIME_ALTER_BASE_URL
import com.yenaly.han1meviewer.HANIME_ALTER_HOSTNAME
import com.yenaly.han1meviewer.HANIME_MAIN_BASE_URL
import com.yenaly.han1meviewer.HANIME_MAIN_HOSTNAME
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.network.HProxySelector
import com.yenaly.han1meviewer.logic.network.HanimeNetwork
import com.yenaly.han1meviewer.logout
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.fragment.IToolbarFragment
import com.yenaly.han1meviewer.ui.view.pref.MaterialDialogPreference
import com.yenaly.han1meviewer.util.createAlertDialog
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.util.showWithBlurEffect
import com.yenaly.yenaly_libs.ActivityManager
import com.yenaly.yenaly_libs.base.preference.MaterialSwitchPreference
import com.yenaly.yenaly_libs.base.settings.YenalySettingsFragment
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/10 010 18:30
 */
class NetworkSettingsFragment : YenalySettingsFragment(R.xml.settings_network),
    IToolbarFragment<SettingsActivity> {

    companion object {
        const val PROXY = "proxy"
        const val PROXY_TYPE = "proxy_type"
        const val PROXY_IP = "proxy_ip"
        const val PROXY_PORT = "proxy_port"
        const val DOMAIN_NAME = "domain_name"
        const val USE_BUILT_IN_HOSTS = "use_built_in_hosts"
    }

    private val proxy
            by safePreference<Preference>(PROXY)
    private val domainName
            by safePreference<MaterialDialogPreference>(DOMAIN_NAME)
    private val useBuiltInHosts
            by safePreference<MaterialSwitchPreference>(USE_BUILT_IN_HOSTS)

    private val proxyDialog by unsafeLazy {
        ProxyDialog(proxy, R.layout.dialog_proxy)
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity).setupToolbar()
    }

    override fun onPreferencesCreated(savedInstanceState: Bundle?) {
        proxy.apply {
            summary = generateProxySummary(
                Preferences.proxyType,
                Preferences.proxyIp,
                Preferences.proxyPort
            )
            setOnPreferenceClickListener {
                proxyDialog.show()
                return@setOnPreferenceClickListener true
            }
        }
        domainName.apply {
            entries = arrayOf(
                "$HANIME_MAIN_HOSTNAME (${getString(R.string.default_)})",
                "$HANIME_ALTER_HOSTNAME (${getString(R.string.alternative)})"
            )
            entryValues = arrayOf(HANIME_MAIN_BASE_URL, HANIME_ALTER_BASE_URL)
            if (value == null) setValueIndex(0)

            setOnPreferenceChangeListener { _, newValue ->
                val origin = Preferences.baseUrl
                if (newValue != origin) {
                    requireContext().showAlertDialog {
                        setCancelable(false)
                        setTitle(R.string.attention)
                        setMessage(getString(R.string.domain_change_tips).trimIndent())
                        setPositiveButton(R.string.confirm) { _, _ ->
                            logout()
                            ActivityManager.restart(killProcess = true)
                        }
                        setNegativeButton(R.string.cancel) { _, _ ->
                            domainName.value = origin
                        }
                    }
                }
                return@setOnPreferenceChangeListener true
            }
        }
        useBuiltInHosts.apply {
            setOnPreferenceChangeListener { _, _ ->
                requireContext().showAlertDialog {
                    setCancelable(false)
                    setTitle(R.string.attention)
                    setMessage(getString(R.string.restart_or_not_working, EMPTY_STRING))
                    setPositiveButton(R.string.confirm) { _, _ ->
                        ActivityManager.restart(killProcess = true)
                    }
                    setNegativeButton(R.string.cancel, null)
                }
                return@setOnPreferenceChangeListener true
            }
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
            dialog = proxyPref.context.createAlertDialog {
                setView(view)
                setCancelable(false)
                setTitle(R.string.proxy)
                setPositiveButton(R.string.confirm, null) // Set to null. We override the onclick.
                setNegativeButton(R.string.cancel, null)
            }
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val ip = etIp.text?.toString().orEmpty()
                    val port = etPort.text?.toString()?.toIntOrNull() ?: -1
                    val isValid = checkValid(ip, port)
                    if (isValid) {
                        val proxyType = proxyType
                        Preferences.preferenceSp.edit(commit = true) {
                            putInt(PROXY_TYPE, proxyType)
                            putString(PROXY_IP, ip)
                            putInt(PROXY_PORT, port)
                        }
                        // 重建相关联的所有网络请求
                        HProxySelector.rebuildNetwork()
                        HanimeNetwork.rebuildNetwork()
                        proxyPref.summary = generateProxySummary(proxyType, ip, port)
                        dialog.dismiss()
                    } else {
                        showShortToast("Invalid IP(v4) or Port(0..65535)")
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun initView() {
            when (Preferences.proxyType) {
                HProxySelector.TYPE_DIRECT -> cgTypes.check(R.id.chip_direct)
                HProxySelector.TYPE_SYSTEM -> cgTypes.check(R.id.chip_system_proxy)
                HProxySelector.TYPE_HTTP -> cgTypes.check(R.id.chip_http)
                HProxySelector.TYPE_SOCKS -> cgTypes.check(R.id.chip_socks)
            }
            val prefIp = Preferences.proxyIp
            val prefPort = Preferences.proxyPort
            if (prefIp.isNotBlank() && prefPort != -1) {
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
            dialog.showWithBlurEffect()
        }
    }

    override fun SettingsActivity.setupToolbar() {
        supportActionBar!!.setTitle(R.string.network_settings)
    }
}
