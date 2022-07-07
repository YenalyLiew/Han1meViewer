package com.yenaly.han1meviewer.logic.state

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/18 018 18:14
 */
sealed class VideoLoadingState<T> {
    data class Success<T>(val info: T) : VideoLoadingState<T>()
    data class Error<T>(val throwable: Throwable) : VideoLoadingState<T>()
    class Loading<T> : VideoLoadingState<T>()
    class NoContent<T> : VideoLoadingState<T>()
}
