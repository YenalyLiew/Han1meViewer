package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.annotation.IdRes
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.entity.download.VideoWithCategories
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/02 002 12:05
 */
class DownloadViewModel(application: Application) : YenalyViewModel(application) {

    @IdRes
    var currentSortOptionId = R.id.sm_sort_by_date_descending

    private val _downloaded = MutableStateFlow(mutableListOf<VideoWithCategories>())
    val downloaded = _downloaded.asStateFlow()

    fun loadAllDownloadingHanime() =
        DatabaseRepo.HanimeDownload.loadAllDownloadingHanime()
            .catch { e -> e.printStackTrace() }
            .flowOn(Dispatchers.IO)

    fun loadAllDownloadedHanime(
        sortedBy: HanimeDownloadEntity.SortedBy = HanimeDownloadEntity.SortedBy.ID,
        ascending: Boolean = false,
    ) {
        viewModelScope.launch {
            DatabaseRepo.HanimeDownload.loadAllDownloadedHanime(sortedBy, ascending)
                .catch { e -> e.printStackTrace() }
                .flowOn(Dispatchers.IO)
                .collect {
                    _downloaded.value = it
                }
        }
    }

    fun updateDownloadHanime(entity: HanimeDownloadEntity) {
        viewModelScope.launch {
            DatabaseRepo.HanimeDownload.update(entity)
        }
    }

    fun deleteDownloadHanimeBy(videoCode: String, quality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HanimeDownload.delete(videoCode, quality)
        }
    }
}