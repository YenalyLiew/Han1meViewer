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

class FavSubViewModel(application: Application) : YenalyViewModel(application) {

    var favVideoPage = 1

    private val _favVideoStateFlow: MutableStateFlow<PageLoadingState<MyListItems<HanimeInfo>>> =
        MutableStateFlow(PageLoadingState.Loading)
    val favVideoStateFlow = _favVideoStateFlow.asStateFlow()

    private val _favVideoFlow = MutableStateFlow(emptyList<HanimeInfo>())
    val favVideoFlow = _favVideoFlow.asStateFlow()

    fun getMyFavVideoItems(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, MyListType.FAV_VIDEO).collect { state ->
                val prev = _favVideoStateFlow.getAndUpdate { state }
                if (prev is PageLoadingState.Loading) _favVideoFlow.value = emptyList()
                _favVideoFlow.update { prevList ->
                    when (state) {
                        is PageLoadingState.Success -> prevList + state.info.hanimeInfo
                        is PageLoadingState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }

    private val _deleteMyFavVideoFlow =
        MutableSharedFlow<WebsiteState<Int>>()
    val deleteMyFavVideoFlow = _deleteMyFavVideoFlow.asSharedFlow()

    fun deleteMyFavVideo(videoCode: String, position: Int) {
        viewModelScope.launch {
            NetworkRepo.deleteMyListItems(
                MyListType.FAV_VIDEO, videoCode,
                position, csrfToken
            ).collect {
                _deleteMyFavVideoFlow.emit(it)
                _favVideoFlow.update { list ->
                    if (it is WebsiteState.Success) {
                        list.toMutableList().apply { removeAt(position) }
                    } else list
                }
            }
        }
    }

    fun clearMyListItems() {
        _favVideoStateFlow.value = PageLoadingState.Loading
    }
}