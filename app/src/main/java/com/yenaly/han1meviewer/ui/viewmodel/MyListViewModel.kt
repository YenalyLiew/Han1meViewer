package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import com.yenaly.han1meviewer.ui.viewmodel.mylist.FavSubViewModel
import com.yenaly.han1meviewer.ui.viewmodel.mylist.PlaylistSubViewModel
import com.yenaly.han1meviewer.ui.viewmodel.mylist.SubscriptionSubViewModel
import com.yenaly.han1meviewer.ui.viewmodel.mylist.WatchLaterSubViewModel
import com.yenaly.yenaly_libs.base.YenalyViewModel

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:46
 */
class MyListViewModel(application: Application) : YenalyViewModel(application) {

    val playlist by sub<PlaylistSubViewModel>()
    val watchLater by sub<WatchLaterSubViewModel>()
    val fav by sub<FavSubViewModel>()
    val subscription by sub<SubscriptionSubViewModel>()
}
