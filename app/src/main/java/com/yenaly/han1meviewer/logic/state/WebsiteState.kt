package com.yenaly.han1meviewer.logic.state

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:20
 */
sealed class WebsiteState<T> {
    data class Success<T>(val info: T) : WebsiteState<T>()
    class Loading<T> : WebsiteState<T>()
    data class Error<T>(val throwable: Throwable) : WebsiteState<T>()
}
