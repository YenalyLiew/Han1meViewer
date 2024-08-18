package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.HANIME_BASE_URL
import com.yenaly.han1meviewer.logic.network.service.HGitHubService
import com.yenaly.han1meviewer.logic.network.service.HanimeBaseService
import com.yenaly.han1meviewer.logic.network.service.HanimeCommentService
import com.yenaly.han1meviewer.logic.network.service.HanimeMyListService
import com.yenaly.han1meviewer.logic.network.service.HanimeSubscriptionService

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:35
 */
object HanimeNetwork {
    var hanimeService = _hanimeService
        private set
    var githubService = _githubService
        private set
    var commentService = _commentService
        private set
    var myListService = _myListService
        private set
    var subscriptionService = _subscriptionService
        private set

    private val _hanimeService
        get() = ServiceCreator.create<HanimeBaseService>(HANIME_BASE_URL)

    private val _githubService
        get() = ServiceCreator.createGitHubApi<HGitHubService>()

    private val _commentService
        get() = ServiceCreator.create<HanimeCommentService>(HANIME_BASE_URL)

    private val _myListService
        get() = ServiceCreator.create<HanimeMyListService>(HANIME_BASE_URL)

    private val _subscriptionService
        get() = ServiceCreator.create<HanimeSubscriptionService>(HANIME_BASE_URL)

    fun rebuildNetwork() {
        ServiceCreator.rebuildOkHttpClient()
        hanimeService = _hanimeService
        githubService = _githubService
        commentService = _commentService
        myListService = _myListService
    }
}