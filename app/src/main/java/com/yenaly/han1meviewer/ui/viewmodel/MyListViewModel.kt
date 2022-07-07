package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.MyListType
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.MyListModel
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:46
 */
class MyListViewModel(application: Application) : YenalyViewModel(application) {

    private val _watchLaterFlow: MutableStateFlow<PageLoadingState<MyListModel>> =
        MutableStateFlow(PageLoadingState.Loading())
    val watchLaterFlow = _watchLaterFlow.asStateFlow()

    private val _favVideoFlow: MutableStateFlow<PageLoadingState<MyListModel>> =
        MutableStateFlow(PageLoadingState.Loading())
    val favVideoFlow = _favVideoFlow.asStateFlow()

    fun getMyWatchLater(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyList(page, MyListType.WATCH_LATER).collect { list ->
                _watchLaterFlow.value = list
            }
        }
    }

    fun getMyFavVideo(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyList(page, MyListType.FAV_VIDEO).collect { list ->
                _favVideoFlow.value = list
            }
        }
    }

    private val _deleteMyWatchLaterFlow =
        MutableSharedFlow<WebsiteState<Unit>>()
    val deleteMyWatchLaterFlow = _deleteMyWatchLaterFlow.asSharedFlow()

    private val _deleteMyFavVideoFlow =
        MutableSharedFlow<WebsiteState<Unit>>()
    val deleteMyFavVideoFlow = _deleteMyFavVideoFlow.asSharedFlow()

    fun deleteMyWatchLater(videoCode: String, csrfToken: String?) {
        viewModelScope.launch {
            NetworkRepo.deleteMyList(MyListType.WATCH_LATER, videoCode, csrfToken).collect {
                _deleteMyWatchLaterFlow.emit(it)
            }
        }
    }

    fun deleteMyFavVideo(videoCode: String, csrfToken: String?) {
        viewModelScope.launch {
            NetworkRepo.deleteMyList(MyListType.FAV_VIDEO, videoCode, csrfToken).collect {
                _deleteMyFavVideoFlow.emit(it)
            }
        }
    }
}