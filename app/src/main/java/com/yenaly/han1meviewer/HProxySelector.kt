package com.yenaly.han1meviewer

import com.yenaly.han1meviewer.ui.fragment.settings.HomeSettingsFragment
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

/**
 * 受 [EhViewer_CN_SXJ 中 EhProxySelector](https://github.com/xiaojieonly/Ehviewer_CN_SXJ/blob/BiLi_PC_Gamer/app/src/main/java/com/hippo/ehviewer/EhProxySelector.java)
 * 的启发，Han1meViewer 也将使用 [HProxySelector] 来实现代理功能。
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/10/07 007 17:32
 */
class HProxySelector : ProxySelector() {

    private var delegation: ProxySelector? = null
    private val alternative: ProxySelector

    init {
        alternative = getDefault() ?: NullProxySelector()
        updateProxy()
    }

    companion object {
        const val TYPE_DIRECT = 0
        const val TYPE_SYSTEM = 1
        const val TYPE_HTTP = 2
        const val TYPE_SOCKS = 3

        private val ipv4Regex =
            Regex("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")

        fun validateIp(ip: String): Boolean {
            return ipv4Regex.matches(ip)
        }

        fun validatePort(port: Int): Boolean {
            return port in 0..65535
        }
    }

    private fun updateProxy() {
        delegation = when (preferenceSp.getInt(HomeSettingsFragment.PROXY_TYPE, TYPE_SYSTEM)) {
            TYPE_DIRECT -> NullProxySelector()
            TYPE_SYSTEM -> alternative
            TYPE_HTTP, TYPE_SOCKS -> null
            else -> NullProxySelector()
        }
    }

    override fun select(uri: URI?): MutableList<Proxy> {
        val type = preferenceSp.getInt(HomeSettingsFragment.PROXY_TYPE, TYPE_SYSTEM)
        if (type == TYPE_HTTP || type == TYPE_SOCKS) {
            val ip = preferenceSp.getString(HomeSettingsFragment.PROXY_IP, EMPTY_STRING)
            val port = preferenceSp.getInt(HomeSettingsFragment.PROXY_PORT, -1)
            if (!ip.isNullOrBlank() && port != -1) {
                val inetAddress = InetAddress.getByName(ip)
                val socketAddress = InetSocketAddress(inetAddress, port)
                return mutableListOf(
                    Proxy(
                        if (type == TYPE_HTTP) Proxy.Type.HTTP else Proxy.Type.SOCKS,
                        socketAddress
                    )
                )
            }
        }

        return delegation?.select(uri) ?: alternative.select(uri)
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        delegation?.select(uri)
    }

    class NullProxySelector : ProxySelector() {
        override fun select(uri: URI?): MutableList<Proxy> = mutableListOf(Proxy.NO_PROXY)

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) = Unit
    }
}