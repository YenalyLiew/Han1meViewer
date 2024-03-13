package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.logic.model.HomePageModel
import com.yenaly.han1meviewer.logic.model.VersionModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:35
 */
class MainViewModel(application: Application) : YenalyViewModel(application), IVersionViewModel {

    private val _versionFlow =
        MutableSharedFlow<WebsiteState<VersionModel>>(replay = 0)
    val versionFlow = _versionFlow.asSharedFlow()

    private val _homePageFlow =
        MutableStateFlow<WebsiteState<HomePageModel>>(WebsiteState.Loading)
    val homePageFlow = _homePageFlow.asStateFlow()

    fun getHomePage() {
        viewModelScope.launch {
            NetworkRepo.getHomePage().collect { homePage ->
                _homePageFlow.value = homePage
            }
        }
    }

    fun deleteWatchHistory(history: WatchHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.WatchHistory.delete(history)
            Log.d("delete_watch_hty", "$history DONE!")
        }
    }

    fun deleteAllWatchHistories() {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.WatchHistory.deleteAll()
            Log.d("del_all_watch_hty", "DONE!")
        }
    }

    fun loadAllWatchHistories() =
        DatabaseRepo.WatchHistory.loadAll()
            .catch { e -> e.printStackTrace() }
            .flowOn(Dispatchers.IO)


    override fun getLatestVersion() {
        viewModelScope.launch {
            NetworkRepo.getLatestVersion().collect {
                _versionFlow.emit(it)
            }
        }
    }

    init {
        getLatestVersion()
    }
}