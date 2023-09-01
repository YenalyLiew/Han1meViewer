package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.ModifiedPlaylistArguments
import com.yenaly.han1meviewer.logic.model.MyListItemsModel
import com.yenaly.han1meviewer.logic.model.MyListType
import com.yenaly.han1meviewer.logic.model.PlaylistsModel
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _watchLaterFlow: MutableSharedFlow<PageLoadingState<MyListItemsModel>> =
        MutableSharedFlow()
    val watchLaterFlow = _watchLaterFlow.asSharedFlow()

    private val _favVideoFlow: MutableSharedFlow<PageLoadingState<MyListItemsModel>> =
        MutableSharedFlow()
    val favVideoFlow = _favVideoFlow.asSharedFlow()

    fun getMyWatchLaterItems(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, MyListType.WATCH_LATER).collect { list ->
                _watchLaterFlow.emit(list)
            }
        }
    }

    fun getMyFavVideoItems(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, MyListType.FAV_VIDEO).collect { list ->
                _favVideoFlow.emit(list)
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
            NetworkRepo.deleteMyListItems(MyListType.WATCH_LATER, videoCode, position, csrfToken)
                .collect {
                    _deleteMyWatchLaterFlow.emit(it)
                }
        }
    }

    fun deleteMyFavVideo(videoCode: String, position: Int) {
        viewModelScope.launch {
            NetworkRepo.deleteMyListItems(MyListType.FAV_VIDEO, videoCode, position, csrfToken)
                .collect {
                    _deleteMyFavVideoFlow.emit(it)
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
        MutableSharedFlow<WebsiteState<PlaylistsModel>>()
    val playlistsFlow = _playlistsFlow.asSharedFlow()

    fun getPlaylists() {
        viewModelScope.launch {
            NetworkRepo.getPlaylists().collect {
                _playlistsFlow.emit(it)
            }
        }
    }

    private val _playlistFlow: MutableSharedFlow<PageLoadingState<MyListItemsModel>> =
        MutableSharedFlow()
    val playlistFlow = _playlistFlow.asSharedFlow()

    fun getPlaylistItems(page: Int, listCode: String) {
        viewModelScope.launch {
            NetworkRepo.getMyListItems(page, listCode).collect {
                _playlistFlow.emit(it)
            }
        }
    }

    private val _deletePlaylistFlow = MutableSharedFlow<WebsiteState<Int>>()
    val deletePlaylistFlow = _deletePlaylistFlow.asSharedFlow()

    fun deletePlaylist(listCode: String, videoCode: String, position: Int) {
        viewModelScope.launch {
            NetworkRepo.deleteMyListItems(listCode, videoCode, position, csrfToken).collect {
                _deletePlaylistFlow.emit(it)
            }
        }
    }

    private val _modifyPlaylistFlow = MutableSharedFlow<WebsiteState<ModifiedPlaylistArguments>>()
    val modifyPlaylistFlow = _modifyPlaylistFlow.asSharedFlow()

    fun modifyPlaylist(listCode: String, title: String, desc: String, delete: Boolean) {
        viewModelScope.launch {
            NetworkRepo.modifyPlaylist(listCode, title, desc, delete, csrfToken).collect {
                _modifyPlaylistFlow.emit(it)
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
}