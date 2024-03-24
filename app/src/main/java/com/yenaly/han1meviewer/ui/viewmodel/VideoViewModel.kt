package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/17 017 19:01
 */
class VideoViewModel(private val application: Application) : YenalyViewModel(application) {

    companion object {
        /**
         * 最小的 HKeyframe 保存間隔，暫定 5s
         */
        const val MIN_H_KEYFRAME_SAVE_INTERVAL = 5_000 // ms
    }

    var videoCode: String = EMPTY_STRING
        set(value) {
            field = value
            // 在這裏初始化所有需要videoCode的方法
            getHanimeVideo(value)
        }

    var hKeyframes: HKeyframeEntity? = null

    var csrfToken: String? = null

    private val _hanimeVideoFlow =
        MutableStateFlow<VideoLoadingState<HanimeVideoModel>>(VideoLoadingState.Loading)
    val hanimeVideoFlow = _hanimeVideoFlow.asStateFlow()

    fun getHanimeVideo(videoCode: String) {
        viewModelScope.launch {
            NetworkRepo.getHanimeVideo(videoCode).collect { video ->
                _hanimeVideoFlow.value = video
            }
        }
    }

    private val _addToFavVideoFlow = MutableSharedFlow<WebsiteState<Unit>>()
    val addToFavVideoFlow = _addToFavVideoFlow.asSharedFlow()

    private val _loadDownloadedFlow = MutableSharedFlow<HanimeDownloadEntity?>()
    val loadDownloadedFlow = _loadDownloadedFlow.asSharedFlow()

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

    private val _modifyMyListFlow = MutableSharedFlow<WebsiteState<Int>>()
    val modifyMyListFlow = _modifyMyListFlow.asSharedFlow()

    fun modifyMyList(
        listCode: String,
        videoCode: String,
        isChecked: Boolean,
        position: Int,
    ) {
        viewModelScope.launch {
            NetworkRepo.addToMyList(listCode, videoCode, isChecked, position, csrfToken).collect {
                _modifyMyListFlow.emit(it)
            }
        }
    }

    fun insertWatchHistory(history: WatchHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.WatchHistory.insert(history)
            Log.d("insert_watch_hty", "$history DONE!")
        }
    }

    fun findDownloadedHanime(videoCode: String, quality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = DatabaseRepo.HanimeDownload.findBy(videoCode, quality)
            _loadDownloadedFlow.emit(info)
        }
    }


    // boolean: 成功 or 失敗，String: 提示信息
    private val _modifyHKeyframeFlow = MutableSharedFlow<Pair<Boolean, String>>()
    val modifyHKeyframeFlow = _modifyHKeyframeFlow.asSharedFlow()

    fun observeKeyframe(videoCode: String) = if (Preferences.hKeyframesEnable)
        DatabaseRepo.HKeyframe.observe(videoCode).flowOn(Dispatchers.IO) else null

    fun appendHKeyframe(videoCode: String, title: String, hKeyframe: HKeyframeEntity.Keyframe) {
        viewModelScope.launch(Dispatchers.IO) {
            run {
                this@VideoViewModel.hKeyframes?.keyframes?.forEach { keyframeInDb ->
                    if (abs(keyframeInDb.position - hKeyframe.position) < MIN_H_KEYFRAME_SAVE_INTERVAL) {
                        Log.d("append_hkeyframe", "time conflict: $keyframeInDb")
                        _modifyHKeyframeFlow.emit(
                            false to application.getString(
                                R.string.interval_must_greater_than_d,
                                MIN_H_KEYFRAME_SAVE_INTERVAL / 1_000L
                            )
                        )
                        return@run
                    }
                }
                DatabaseRepo.HKeyframe.appendKeyframe(videoCode, title, hKeyframe)
                Log.d("append_hkeyframe", "$hKeyframe DONE!")
                _modifyHKeyframeFlow.emit(true to application.getString(R.string.add_success))
            }
        }
    }

    fun removeHKeyframe(videoCode: String, hKeyframe: HKeyframeEntity.Keyframe) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.removeKeyframe(videoCode, hKeyframe)
            Log.d("remove_hkeyframe", "$hKeyframe DONE!")
            _modifyHKeyframeFlow.emit(true to application.getString(R.string.delete_success))
        }
    }

    fun modifyHKeyframe(
        videoCode: String,
        oldKeyframe: HKeyframeEntity.Keyframe, keyframe: HKeyframeEntity.Keyframe,
    ) {
        viewModelScope.launch {
            DatabaseRepo.HKeyframe.modifyKeyframe(videoCode, oldKeyframe, keyframe)
            Log.d("modify_hkeyframe", "$keyframe DONE!")
            _modifyHKeyframeFlow.emit(true to application.getString(R.string.modify_success))
        }
    }
}