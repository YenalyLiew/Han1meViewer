package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.HANIME_BASE_URL

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:35
 */
object HanimeNetwork {
    val hanimeService = ServiceCreator.create<HanimeService>(HANIME_BASE_URL)
}