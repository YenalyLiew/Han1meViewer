package com.yenaly.han1meviewer.ui.viewmodel.mylist

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.MyListItems
import com.yenaly.han1meviewer.logic.model.MyListType
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.viewmodel.AppViewModel.csrfToken
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WatchLaterSubViewModel(application: Application) : YenalyViewModel(application) {

    var watchLaterPage = 1

    private val _watchLaterStateFlow: MutableStateFlow<PageLoadingState<MyListItems<HanimeInfo>>> =
        MutableStateFlow(PageLoadingState.Loading)
    val watchLaterStateFlow = _watchLaterStateFlow.asStateFlow()

    private val _watchLaterFlow = MutableStateFlow(emptyList<HanimeInfo>())
    val watchLaterFlow = _watchLaterFlow.asStateFlow()

    fun getMyWatchLaterItems(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, MyListType.WATCH_LATER).collect { state ->
                val prev = _watchLaterStateFlow.getAndUpdate { state }
                if (prev is PageLoadingState.Loading) _watchLaterFlow.value = emptyList()
                _watchLaterFlow.update { prevList ->
                    when (state) {
                        is PageLoadingState.Success -> prevList + state.info.hanimeInfo
                        is PageLoadingState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }

    private val _deleteMyWatchLaterFlow =
        MutableSharedFlow<WebsiteState<Int>>()
    val deleteMyWatchLaterFlow = _deleteMyWatchLaterFlow.asSharedFlow()

    fun deleteMyWatchLater(videoCode: String, position: Int) {
        viewModelScope.launch {
            NetworkRepo.deleteMyListItems(
                MyListType.WATCH_LATER, videoCode,
                position, csrfToken
            ).collect {
                _deleteMyWatchLaterFlow.emit(it)
                _watchLaterFlow.update { list ->
                    if (it is WebsiteState.Success) {
                        list.toMutableList().apply { removeAt(position) }
                    } else list
                }
            }
        }
    }

    fun clearMyListItems() {
        _watchLaterStateFlow.value = PageLoadingState.Loading
    }
}