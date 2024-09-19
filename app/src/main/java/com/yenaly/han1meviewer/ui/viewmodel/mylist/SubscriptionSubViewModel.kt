package com.yenaly.han1meviewer.ui.viewmodel.mylist

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.MyListItems
import com.yenaly.han1meviewer.logic.model.Subscription
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.ui.viewmodel.IHCsrfToken
import com.yenaly.han1meviewer.ui.viewmodel.MyListViewModel
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionSubViewModel(
    application: Application
) : YenalyViewModel(application), IHCsrfToken {

    override var csrfToken: String?
        get() = getParent<MyListViewModel>()?.csrfToken
        set(_) = error("Setting csrfToken here is not allowed")

    var subscriptionPage = 1

    private val _subscriptionStateFlow =
        MutableStateFlow<PageLoadingState<MyListItems<Subscription>>>(PageLoadingState.Loading)
    val subscriptionStateFlow = _subscriptionStateFlow.asStateFlow()

    private val _subscriptionFlow =
        MutableStateFlow(emptyList<Subscription>())
    val subscriptionFlow = _subscriptionFlow.asStateFlow()

    // 暂时就加载一页，不做分页
    fun getSubscriptionsWithSinglePage() {
        viewModelScope.launch {
            NetworkRepo.getSubscriptions(1).collect { state ->
                _subscriptionStateFlow.value = state
                if (state is PageLoadingState.Success) {
                    _subscriptionFlow.value = state.info.hanimeInfo
                }
            }
        }
    }

    fun getSubscriptions(page: Int) {
        viewModelScope.launch {
            NetworkRepo.getSubscriptions(page).collect { state ->
                val prev = _subscriptionStateFlow.getAndUpdate { state }
                if (prev is PageLoadingState.Loading) _subscriptionFlow.value = emptyList()
                _subscriptionFlow.update { prevList ->
                    when (state) {
                        is PageLoadingState.Success -> prevList + state.info.hanimeInfo
                        is PageLoadingState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }
}