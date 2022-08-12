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
 * A mediator to link a BottomNavigationView with a ViewPager2. For FragmentActivity only!
 *
 * Instantiating a BottomNavigationViewMediator will only create the mediator object,
 * you must call [attach] on it first to link the BottomNavigationView and the ViewPager2 together.
 *
 * @param fragmentActivity (optional)
 * @param bottomNavigationView
 * @param viewPager2
 * @param itemIdWithFragmentList fragment item id with fragment list
 * @param slide if ViewPager2 needs to slide
 * @param smoothScroll if ViewPager2 scrolls smoothly when BottomNavView is selected
 *
 * @author Yenaly Liew
 * @Time : 2022/06/03 003 11:21
 * @Description : Description...
 */
@Suppress("unused")
class BottomNavigationViewMediator @JvmOverloads constructor(
    private val fragmentActivity: FragmentActivity,
    private val bottomNavigationView: BottomNavigationView,
    private val viewPager2: ViewPager2,
    private val itemIdWithFragmentList: List<Pair<Int, Fragment>>,
    var slide: Boolean = true,
    var smoothScroll: Boolean = true
) {

    var currentFragment: Fragment? = null
        private set

    private var listener: OnFragmentChangedListener? = null

    private val fragmentList = itemIdWithFragmentList.map { it.second }

    // 将list存到SparseArray里，方便后续直接通过itemId拿Fragment
    private val itemIdWithIndexMap = SparseIntArray().also { map ->
        itemIdWithFragmentList.forEachIndexed { index, pair ->
            map[pair.first] = index
        }
    }

    private var attached = false
    private var viewPager2Adapter: RecyclerView.Adapter<*>? = null

    private var onItemSelectedListener: NavigationBarView.OnItemSelectedListener? = null
    private var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    @JvmOverloads
    constructor(
        bottomNavigationView: BottomNavigationView,
        viewPager2: ViewPager2,
        itemIdWithFragmentList: List<Pair<Int, Fragment>>,
        slide: Boolean = true,
        smoothScroll: Boolean = true
    ) : this(
        viewPager2.context.activity as? FragmentActivity
            ?: throw IllegalStateException("context cannot be cast to FragmentActivity!"),
        bottomNavigationView,
        viewPager2,
        itemIdWithFragmentList,
        slide,
        smoothScroll
    )

    fun attach(): BottomNavigationViewMediator {
        if (attached) {
            throw IllegalStateException("${javaClass.simpleName} is already attached")
        }
        if (itemIdWithFragmentList.isEmpty()) {
            throw IllegalStateException("fragment list could not be empty!")
        }
        viewPager2Adapter = object : FragmentStateAdapter(fragmentActivity) {
            override fun getItemCount(): Int {
                return itemIdWithFragmentList.size
            }

            override fun createFragment(position: Int): Fragment {
                val softCopyFragmentList: MutableList<Fragment> = ArrayList(fragmentList)
                return softCopyFragmentList[position]
            }
        }
        attached = true

        onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
            val currentItem = itemIdWithIndexMap[item.itemId]
            viewPager2.setCurrentItem(currentItem, smoothScroll)
            currentFragment = itemIdWithFragmentList[currentItem].second
            // listener?.onFragmentSelected(itemIdWithFragmentList[currentItem].second)
            true
        }
        onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentItem = itemIdWithFragmentList[position]
                bottomNavigationView.selectedItemId = currentItem.first
                currentFragment = currentItem.second
                listener?.onFragmentChanged(currentItem.second)
            }
        }

        viewPager2.isUserInputEnabled = slide
        viewPager2.adapter = viewPager2Adapter
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener!!)
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback!!)

        return this
    }

    /**
     * call this to jump to specific fragment
     *
     * @return fragment
     */
    @JvmOverloads
    fun jumpToFragment(@IdRes fragmentItemId: Int, smoothScroll: Boolean = false): Fragment {
        if (onItemSelectedListener == null || onPageChangeCallback == null) {
            throw IllegalStateException("must call attach() first!")
        }
        val fragmentIndex = itemIdWithIndexMap[fragmentItemId]
        val fragment = itemIdWithFragmentList[fragmentIndex].second
        viewPager2.setCurrentItem(fragmentIndex, smoothScroll)
        bottomNavigationView.selectedItemId = fragmentItemId
        currentFragment = fragment
        listener?.onFragmentChanged(fragment)
        return fragment
    }

    /**
     * Set on a callback interface that is optionally
     * implemented to listen the latest selected fragment.
     */
    fun setOnFragmentChangedListener(listener: OnFragmentChangedListener) {
        this.listener = listener
    }

    /**
     * A callback interface that is optionally implemented to listen the latest selected fragment.
     */
    fun interface OnFragmentChangedListener {
        fun onFragmentChanged(currentFragment: Fragment)
    }
}