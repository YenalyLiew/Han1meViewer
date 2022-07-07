package com.yenaly.han1meviewer.logic.state

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/10 010 16:30
 */
sealed class PageLoadingState<T> {
    data class Success<T>(val info: T) : PageLoadingState<T>()
    class Loading<T> : PageLoadingState<T>()
    class NoMoreData<T> : PageLoadingState<T>()
    data class Error<T>(val throwable: Throwable) : PageLoadingState<T>()
}
