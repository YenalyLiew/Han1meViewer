package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.HANIME_ALTER_HOSTNAME
import com.yenaly.han1meviewer.HANIME_MAIN_HOSTNAME
import com.yenaly.han1meviewer.Preferences
import okhttp3.Dns
import java.net.InetAddress

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/10 010 17:01
 */
class HDns : Dns {

    private val dnsMap = mutableMapOf<String, List<InetAddress>>()

    private val useBuiltInHosts = Preferences.useBuiltInHosts

    init {
        if (useBuiltInHosts) {
            dnsMap[HANIME_MAIN_HOSTNAME] = listOf(
                "104.26.0.18", "104.26.1.18", "172.67.69.183",
                "2400:cb00:2048:1::681a:12"
            )
            dnsMap[HANIME_ALTER_HOSTNAME] = listOf(
                "104.26.0.18", "104.26.1.18", "172.67.167.30",
                "172.67.69.183", "188.114.96.2",
                "2001::1f0d:5628"
            )
        }
    }

    companion object {

        /**
         * 添加DNS
         */
        private operator fun MutableMap<String, List<InetAddress>>.set(
            host: String, ips: List<String>,
        ) {
            this[host] = ips.map {
                InetAddress.getByAddress(host, InetAddress.getByName(it).address)
            }
        }
    }

    override fun lookup(hostname: String): List<InetAddress> {
        return if (useBuiltInHosts) dnsMap[hostname] ?: Dns.SYSTEM.lookup(hostname)
        else Dns.SYSTEM.lookup(hostname)
    }

}