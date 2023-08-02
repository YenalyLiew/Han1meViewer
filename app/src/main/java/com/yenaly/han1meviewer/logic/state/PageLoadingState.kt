package com.yenaly.han1meviewer.logic.state

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/10 010 16:30
 */
sealed class PageLoadingState<out T> {
    data class Success<out T>(val info: T) : PageLoadingState<T>()
    data object Loading : PageLoadingState<Nothing>()
    data object NoMoreData : PageLoadingState<Nothing>()
    data class Error(val throwable: Throwable) : PageLoadingState<Nothing>()
}
