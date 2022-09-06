package com.yenaly.yenaly_libs.utils.view

import android.util.SparseIntArray
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.yenaly.yenaly_libs.utils.activity

/**
 * @author Yenaly Liew
 * @time 2022/08/03 003 21:04
 */
class BottomNavViewMediator(
    private val bottomNavigationView: BottomNavigationView,
    private val viewPager2: ViewPager2,
    private val itemIds: IntArray,
    var smoothScroll: Boolean = true,
    private val interfaceCombine: ICombineItemIdWithFragment,
) {

    private val fragmentActivity = bottomNavigationView.context.activity as? FragmentActivity
        ?: throw IllegalStateException("context cannot be cast to FragmentActivity!")

    private var currentFragment: Fragment? = null
    private var onFragmentChangedListener: OnFragmentChangedListener? = null

    private val itemIdsSparseArray = SparseIntArray()

    private var attached = false

    private var viewPager2Adapter: RecyclerView.Adapter<*>? = null
    private var onItemSelectedListener: NavigationBarView.OnItemSelectedListener? = null
    private var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    fun attach(): BottomNavViewMediator {
        check(!attached) { "${javaClass.simpleName} is already attached" }
        check(itemIds.isNotEmpty()) { "fragment list could not be empty!" }

        itemIds.forEachIndexed { index, itemId -> itemIdsSparseArray[itemId] = index }

        viewPager2Adapter = object : FragmentStateAdapter(fragmentActivity) {
            override fun getItemCount() = itemIds.size
            override fun createFragment(position: Int): Fragment {
                return interfaceCombine.combine(itemIds[position])
                    ?: throw IllegalStateException("Do you actually set the proper fragment?")
            }
        }
        attached = true

        onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
            val currentIndex = itemIdsSparseArray[item.itemId]
            viewPager2.setCurrentItem(currentIndex, smoothScroll)
            currentFragment = interfaceCombine.combine(item.itemId)
            onFragmentChangedListener?.onFragmentChanged(currentFragment!!)
            return@OnItemSelectedListener true
        }
        onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.selectedItemId = itemIds[position]
            }
        }

        viewPager2.adapter = viewPager2Adapter
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener!!)
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback!!)

        return this
    }

    fun toFragment(@IdRes fragmentItemId: Int, smoothScroll: Boolean = false) {
        check(attached) { "must call attach() first!" }
        val fragmentIndex = itemIdsSparseArray[fragmentItemId]
        viewPager2.setCurrentItem(fragmentIndex, smoothScroll)
        bottomNavigationView.selectedItemId = fragmentItemId
    }

    /**
     * Set on a callback interface that is optionally
     * implemented to listen the latest selected fragment.
     */
    fun setOnFragmentChangedListener(listener: OnFragmentChangedListener) {
        this.onFragmentChangedListener = listener
    }

    fun interface ICombineItemIdWithFragment {
        fun combine(@IdRes itemId: Int): Fragment?
    }

    fun interface OnFragmentChangedListener {
        fun onFragmentChanged(currentFragment: Fragment)
    }
}