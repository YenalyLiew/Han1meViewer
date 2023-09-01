package com.yenaly.han1meviewer.ui.fragment.home

import androidx.appcompat.app.AppCompatActivity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/27 027 16:29
 */
interface IToolbarFragment<T : AppCompatActivity> {
    fun T.setupToolbar()
}