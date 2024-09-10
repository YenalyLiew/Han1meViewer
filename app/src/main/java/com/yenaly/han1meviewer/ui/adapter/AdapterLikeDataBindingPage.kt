package com.yenaly.han1meviewer.ui.adapter

import androidx.databinding.ViewDataBinding

/**
 * 利用 Adapter 性质的页面，同时运用 DataBinding
 */
interface AdapterLikeDataBindingPage<DB : ViewDataBinding> {
    var binding: DB?
}