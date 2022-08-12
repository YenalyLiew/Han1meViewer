package com.yenaly.han1meviewer.logic.state

import java.io.File

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 19:21
 */
sealed class DownloadState {
    object None : DownloadState()
    data class Progress(val value: Int) : DownloadState()
    data class Error(val throwable: Throwable) : DownloadState()
    data class Done(val file: File) : DownloadState()
}
