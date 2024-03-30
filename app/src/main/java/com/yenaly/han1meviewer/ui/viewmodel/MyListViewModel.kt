package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.ModifiedPlaylistArguments
import com.yenaly.han1meviewer.logic.model.MyListItems
import com.yenaly.han1meviewer.logic.model.MyListType
import com.yenaly.han1meviewer.logic.model.Playlists
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/04 004 22:46
 */
class MyListViewModel(application: Application) : YenalyViewModel(application) {

    var csrfToken: String? = null

    //<editor-fold desc="Watch Later & Fav">

    var watchLaterPage = 1
    var favVideoPage = 1

    private val _watchLaterStateFlow: MutableStateFlow<PageLoadingState<MyListItems>> =
        MutableStateFlow(PageLoadingState.Loading)
    val watchLaterStateFlow = _watchLaterStateFlow.asStateFlow()

    private val _watchLaterFlow = MutableStateFlow(emptyList<HanimeInfo>())
    val watchLaterFlow = _watchLaterFlow.asStateFlow()

    private val _favVideoStateFlow: MutableStateFlow<PageLoadingState<MyListItems>> =
        MutableStateFlow(PageLoadingState.Loading)
    val favVideoStateFlow = _favVideoStateFlow.asStateFlow()

    private val _favVideoFlow = MutableStateFlow(emptyList<HanimeInfo>())
    val favVideoFlow = _favVideoFlow.asStateFlow()

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

    private val _deleteMyWatchLaterFlow =
        MutableSharedFlow<WebsiteState<Int>>()
    val deleteMyWatchLaterFlow = _deleteMyWatchLaterFlow.asSharedFlow()

    private val _deleteMyFavVideoFlow =
        MutableSharedFlow<WebsiteState<Int>>()
    val deleteMyFavVideoFlow = _deleteMyFavVideoFlow.asSharedFlow()

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

    //</editor-fold>

    //<editor-fold desc="Playlist">

    var playlistPage = 1
    var playlistCode: String? = null
    var playlistTitle: String? = null
    var playlistDesc: String? = null

    private val _playlistsFlow =
        MutableStateFlow<WebsiteState<Playlists>>(WebsiteState.Loading)
    val playlistsFlow = _playlistsFlow.asStateFlow()

    fun getPlaylists() {
        viewModelScope.launch {
            NetworkRepo.getPlaylists().collect {
                _playlistsFlow.value = it
            }
        }
    }

    private val _playlistStateFlow =
        MutableStateFlow<PageLoadingState<MyListItems>>(PageLoadingState.Loading)
    val playlistStateFlow = _playlistStateFlow.asStateFlow()

    private val _playlistFlow = MutableStateFlow(emptyList<HanimeInfo>())
    val playlistFlow = _playlistFlow.asStateFlow()

    fun getPlaylistItems(page: Int, listCode: String) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, listCode).collect { state ->
                val prev = _playlistStateFlow.getAndUpdate { state }
                if (prev is PageLoadingState.Loading) _playlistFlow.value = emptyList()
                _playlistFlow.update { prevList ->
                    when (state) {
                        is PageLoadingState.Success -> prevList + state.info.hanimeInfo
                        is PageLoadingState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }

    private val _deleteFromPlaylistFlow = MutableSharedFlow<WebsiteState<Int>>()
    val deleteFromPlaylistFlow = _deleteFromPlaylistFlow.asSharedFlow()

    fun deleteFromPlaylist(listCode: String, videoCode: String, position: Int) {
        viewModelScope.launch {
            NetworkRepo.deleteMyListItems(listCode, videoCode, position, csrfToken).collect {
                _deleteFromPlaylistFlow.emit(it)
                _playlistFlow.update { prevList ->
                    if (it is WebsiteState.Success) {
                        prevList.toMutableList().apply { removeAt(position) }
                    } else prevList
                }
            }
        }
    }

    private val _modifyPlaylistFlow = MutableSharedFlow<WebsiteState<ModifiedPlaylistArguments>>()
    val modifyPlaylistFlow = _modifyPlaylistFlow.asSharedFlow()

    fun modifyPlaylist(listCode: String, title: String, desc: String, delete: Boolean) {
        viewModelScope.launch {
            NetworkRepo.modifyPlaylist(listCode, title, desc, delete, csrfToken).collect {
                _modifyPlaylistFlow.emit(it)
                if (delete) {
                    clearMyListItems(listCode)
                }
            }
        }
    }

    private val _createPlaylistFlow = MutableSharedFlow<WebsiteState<Unit>>()
    val createPlaylistFlow = _createPlaylistFlow.asSharedFlow()

    fun createPlaylist(title: String, description: String) {
        viewModelScope.launch {
            NetworkRepo.createPlaylist(EMPTY_STRING, title, description, csrfToken).collect {
                _createPlaylistFlow.emit(it)
            }
        }
    }

    //</editor-fold>

    fun clearMyListItems(typeOrCode: Any?) {
        when (typeOrCode) {
            MyListType.WATCH_LATER -> _watchLaterStateFlow.update { PageLoadingState.Loading }
            MyListType.FAV_VIDEO -> _favVideoStateFlow.update { PageLoadingState.Loading }
            else -> _playlistStateFlow.update { PageLoadingState.Loading }
        }
    }
}