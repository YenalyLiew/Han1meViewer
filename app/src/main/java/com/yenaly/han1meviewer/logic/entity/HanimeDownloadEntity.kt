package com.yenaly.han1meviewer.logic.entity

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/18 018 21:50
 */
@Entity
data class HanimeDownloadEntity(
    /**
     * 封面地址
     */
    var coverUrl: String,
    /**
     * 影片标题
     */
    var title: String,
    /**
     * 添加日期
     */
    var addDate: Long,
    /**
     * 影片代码
     */
    var videoCode: String,
    /**
     * 影片存储在本地的位置
     */
    var videoUri: String,
    /**
     * 影片质量
     */
    var quality: String,

    /**
     * 影片下载地址
     */
    var videoUrl: String,
    /**
     * 影片长度
     */
    var length: Long,
    /**
     * 影片已下载长度
     */
    var downloadedLength: Long,
    /**
     * 是否正在下载
     */
    var isDownloading: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
) {
    /**
     * 下载进度
     */
    @get:IntRange(from = 0, to = 100)
    val progress get() = (downloadedLength * 100 / length).toInt()

    /**
     * 是否已下载完成
     */
    val isDownloaded get() = downloadedLength == length
}