package com.yenaly.yenaly_libs.utils.view

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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

@Suppress("NOTHING_TO_INLINE")
inline fun TabLayout.attach(
    viewPager2: ViewPager2,
    tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
): TabLayoutMediator {
    return TabLayoutMediator(this, viewPager2, tabConfigurationStrategy).also { it.attach() }
}