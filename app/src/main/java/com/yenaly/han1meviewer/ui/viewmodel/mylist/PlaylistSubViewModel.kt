package com.yenaly.han1meviewer.ui.viewmodel.mylist

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.ModifiedPlaylistArgs
import com.yenaly.han1meviewer.logic.model.MyListItems
import com.yenaly.han1meviewer.logic.model.Playlists
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

class PlaylistSubViewModel(application: Application) : YenalyViewModel(application) {

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
        MutableStateFlow<PageLoadingState<MyListItems<HanimeInfo>>>(PageLoadingState.Loading)
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

    private val _modifyPlaylistFlow = MutableSharedFlow<WebsiteState<ModifiedPlaylistArgs>>()
    val modifyPlaylistFlow = _modifyPlaylistFlow.asSharedFlow()

    fun modifyPlaylist(listCode: String, title: String, desc: String, delete: Boolean) {
        viewModelScope.launch {
            NetworkRepo.modifyPlaylist(listCode, title, desc, delete, csrfToken).collect {
                _modifyPlaylistFlow.emit(it)
                if (delete) {
                    clearMyListItems()
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

    fun clearMyListItems() {
        _playlistStateFlow.value = PageLoadingState.Loading
    }
}