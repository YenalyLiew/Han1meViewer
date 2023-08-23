package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.logic.model.HanimeVideoModel
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/17 017 19:01
 */
class VideoViewModel(application: Application) : YenalyViewModel(application) {

    lateinit var videoCode: String

    var csrfToken: String? = null

    private val _hanimeVideoFlow =
        MutableStateFlow<VideoLoadingState<HanimeVideoModel>>(VideoLoadingState.Loading)
    val hanimeVideoFlow = _hanimeVideoFlow.asStateFlow()

    private val _addToFavVideoFlow = MutableSharedFlow<WebsiteState<Unit>>()
    val addToFavVideoFlow = _addToFavVideoFlow.asSharedFlow()

    private val _loadDownloadedFlow = MutableSharedFlow<HanimeDownloadEntity?>()
    val loadDownloadedFlow = _loadDownloadedFlow.asSharedFlow()

    fun getHanimeVideo(videoCode: String) {
        viewModelScope.launch {
            NetworkRepo.getHanimeVideo(videoCode).collect { video ->
                _hanimeVideoFlow.value = video
            }
        }
    }

    fun addToFavVideo(
        videoCode: String,
        currentUserId: String?,
    ) = modifyFavVideoInternal(videoCode, likeStatus = false, currentUserId)

    fun removeFromFavVideo(
        videoCode: String,
        currentUserId: String?,
    ) = modifyFavVideoInternal(videoCode, likeStatus = true, currentUserId)

    private fun modifyFavVideoInternal(
        videoCode: String,
        likeStatus: Boolean,
        currentUserId: String?,
    ) {
        viewModelScope.launch {
            NetworkRepo.addToMyFavVideo(videoCode, likeStatus, currentUserId, csrfToken)
                .collect {
                    _addToFavVideoFlow.emit(it)
                }
        }
    }

    fun insertWatchHistory(history: WatchHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.WatchHistory.insert(history)
            Log.d("insert_watch_hty", "$history DONE!")
        }
    }

    fun findDownloadedHanimeByVideoCodeAndQuality(videoCode: String, quality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = DatabaseRepo.HanimeDownload.findBy(videoCode, quality)
            _loadDownloadedFlow.emit(info)
        }
    }
}