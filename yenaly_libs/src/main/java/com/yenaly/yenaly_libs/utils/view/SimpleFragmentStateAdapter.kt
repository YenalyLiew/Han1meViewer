package com.yenaly.yenaly_libs.utils.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @author Yenaly Liew
 * @time 2022/11/09 009 14:05
 */
class SimpleFragmentStateAdapter : FragmentStateAdapter {

    private val newFragmentList = mutableListOf<NewFragment>()

    constructor(fragmentActivity: FragmentActivity) :
            super(fragmentActivity)

    constructor(fragment: Fragment) :
            super(fragment)

    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
            super(fragmentManager, lifecycle)

    override fun getItemCount(): Int {
        return newFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return newFragmentList[position].invoke()
    }

    fun addFragment(newFragment: NewFragment) {
        newFragmentList.add(newFragment)
    }
}