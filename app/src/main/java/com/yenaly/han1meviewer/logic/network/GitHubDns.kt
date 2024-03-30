package com.yenaly.han1meviewer.logic.network

import okhttp3.Dns
import java.net.InetAddress

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/29 029 17:14
 */
object GitHubDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return when (hostname) {
            "api.github.com" -> listOf(InetAddress.getByName("140.82.121.6"))
            "github.com" -> listOf(InetAddress.getByName("140.82.121.4"))
            else -> Dns.SYSTEM.lookup(hostname)
        }
    }
}