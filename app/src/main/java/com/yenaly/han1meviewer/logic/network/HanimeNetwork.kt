package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.HANIME_BASE_URL
import com.yenaly.han1meviewer.logic.network.service.HanimeCommentService
import com.yenaly.han1meviewer.logic.network.service.HanimeBaseService
import com.yenaly.han1meviewer.logic.network.service.HanimeVersionService

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:35
 */
object HanimeNetwork {
    val hanimeService = ServiceCreator.create<HanimeBaseService>(HANIME_BASE_URL)
    val versionService = ServiceCreator.createVersion<HanimeVersionService>()
    val commentService = ServiceCreator.create<HanimeCommentService>(HANIME_BASE_URL)
}