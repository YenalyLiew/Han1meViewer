package com.yenaly.yenaly_libs.utils.view

import android.util.SparseArray
import android.util.SparseIntArray
import androidx.annotation.IdRes
import androidx.core.util.isNotEmpty
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.yenaly.yenaly_libs.utils.activity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/11/09 009 14:39
 */
class SimpleBottomNavViewMediator constructor(
    private val bottomNavigationView: BottomNavigationView,
    private val viewPager2: ViewPager2,
    var smoothScroll: Boolean = true
) {

    private val fragmentActivity = bottomNavigationView.context.activity as? FragmentActivity
        ?: throw IllegalStateException("context cannot be cast to FragmentActivity!")

    private var currentFragment: Fragment? = null
    private var onFragmentChangedListener: OnFragmentChangedListener? = null

    private var attached = false

    private var index = 0
    private val newFragmentMap = SparseArray<NewFragment>()
    private val indexMap = SparseIntArray()
    private val newFragmentList = mutableListOf<Pair<Int, NewFragment>>()

    private var viewPager2Adapter: RecyclerView.Adapter<*>? = null
    private var onItemSelectedListener: NavigationBarView.OnItemSelectedListener? = null
    private var onPageChangeCallback: OnPageChangeCallback? = null

    fun attach() = apply {
        check(!attached) { "${javaClass.simpleName} is already attached" }
        check(newFragmentMap.isNotEmpty()) { "fragment list could not be empty!" }

        viewPager2Adapter = object : FragmentStateAdapter(fragmentActivity) {
            override fun getItemCount(): Int {
                return index
            }

            override fun createFragment(position: Int): Fragment {
                return newFragmentList[position].second.invoke()
            }
        }

        onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
            val currentIndex = indexMap[item.itemId]
            viewPager2.setCurrentItem(currentIndex, smoothScroll)
            return@OnItemSelectedListener true
        }

        onPageChangeCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.selectedItemId = newFragmentList[position].first
                onFragmentChangedListener?.onFragmentChanged(
                    fragmentActivity.supportFragmentManager.findFragmentByTag(
                        "f${position}"
                    )!!.also { currentFragment = it }
                )
            }
        }

        viewPager2.adapter = viewPager2Adapter
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener!!)
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback!!)

        attached = true
    }

    fun detach() = apply {
        TODO("I am lazy!")
    }

    /**
     * For example:
     *
     * `R.id.xxx with { XXFragment() }`
     */
    infix fun @receiver:IdRes Int.with(newFragment: NewFragment) {
        newFragmentMap[this] = newFragment
        indexMap[this] = index
        newFragmentList += this to newFragment
        index++
    }

    /**
     * Set on a callback interface that is optionally
     * implemented to listen the latest selected fragment.
     */
    fun setOnFragmentChangedListener(listener: OnFragmentChangedListener) {
        this.onFragmentChangedListener = listener
    }

    fun interface OnFragmentChangedListener {
        fun onFragmentChanged(currentFragment: Fragment)
    }
}