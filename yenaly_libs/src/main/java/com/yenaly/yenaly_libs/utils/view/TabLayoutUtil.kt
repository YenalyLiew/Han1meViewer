package com.yenaly.yenaly_libs.utils.view

import com.google.android.material.tabs.TabLayout

inline fun TabLayout.addOnTabSelectedListener(
    crossinline onTabUnselected: (TabLayout.Tab) -> Unit = {},
    crossinline onTabReselected: (TabLayout.Tab) -> Unit = {},
    crossinline onTabSelect: (TabLayout.Tab) -> Unit,
) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onTabSelect.invoke(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            onTabUnselected.invoke(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            onTabReselected.invoke(tab)
        }
    })
}