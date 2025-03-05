package com.yenaly.han1meviewer.logic.entity.download

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yenaly.han1meviewer.HFileManager
import com.yenaly.han1meviewer.logic.state.DownloadState

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/18 018 21:50
 */
@Entity
@TypeConverters(HanimeDownloadEntity.StateTypeConverter::class)
data class HanimeDownloadEntity(
    /**
     * 封面地址
     */
    val coverUrl: String,
    /**
     * 封面图片本地地址
     */
    var coverUri: String?,
    /**
     * 影片标题
     */
    val title: String,
    /**
     * 添加日期
     */
    val addDate: Long,
    /**
     * 影片代码
     */
    val videoCode: String,
    /**
     * 影片存储在本地的位置
     */
    val videoUri: String,
    /**
     * 影片质量
     */
    val quality: String,

    /**
     * 影片下载地址
     */
    val videoUrl: String,
    /**
     * 影片长度
     */
    val length: Long,
    /**
     * 影片已下载长度
     */
    val downloadedLength: Long,
//    /**
//     * 是否正在下载
//     */
//    val isDownloading: Boolean = false,
    /**
     * 当前状态
     */
    val state: DownloadState = DownloadState.Unknown,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {
    /**
     * 下载进度
     */
    @get:IntRange(from = 0, to = 100)
    val progress get() = (downloadedLength * 100 / length).toInt()

    /**
     * 是否已下载完成
     */
    val isDownloaded get() = state == DownloadState.Finished

    val isDownloading get() = state == DownloadState.Downloading

    val suffix get() = videoUri.substringAfterLast(".", HFileManager.DEF_VIDEO_TYPE)

    /**
     * 排序方式
     */
    enum class SortedBy {
        ID, TITLE
    }

    class StateTypeConverter {
        @TypeConverter
        fun from(state: DownloadState): Int = state.mask

        @TypeConverter
        fun to(state: Int): DownloadState = DownloadState.from(state)
    }
}