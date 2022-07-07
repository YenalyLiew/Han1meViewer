@file:JvmName("ViewPagerUtil")
@file:Suppress("deprecation")

package com.yenaly.yenaly_libs.utils.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * [ViewPager2]与[BottomNavigationView]配合使用来实现页面切换。
 *
 * @param bnv BottomNavigationView
 * @param fragmentActivity fragmentActivity
 * @param itemIdWithFragmentList item id with fragment list
 * @param listener listen current fragment
 *
 * @author Yenaly Liew
 */
@Deprecated("use BottomNavigationViewMediator instead.")
@JvmOverloads
fun ViewPager2.setUpWithBottomNavigationView(
    bnv: BottomNavigationView,
    fragmentActivity: FragmentActivity,
    itemIdWithFragmentList: List<Pair<Int, Fragment>>,
    listener: OnFragmentSelectedListener? = null
) {
    // 将list存到map里，方便后续直接通过itemId拿Fragment
    val itemIdWithIndexMap = hashMapOf<Int, Int>()
    itemIdWithFragmentList.forEachIndexed { index, pair ->
        itemIdWithIndexMap[pair.first] = index
    }

    this.adapter = object : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return itemIdWithFragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return itemIdWithFragmentList[position].second
        }
    }

    bnv.setOnItemSelectedListener {
        val currentItem = itemIdWithIndexMap[it.itemId]!!
        this.currentItem = currentItem
        listener?.onFragmentSelected(itemIdWithFragmentList[currentItem].second)
        true
    }

    this.registerOnPageChangeCallback(
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentItem = itemIdWithFragmentList[position]
                bnv.selectedItemId = currentItem.first
                listener?.onFragmentSelected(currentItem.second)
            }
        }
    )
}

/**
 * 如果直接给ViewPager2设置overScrollMode会无效，
 * 这种方式能设置有效的overScrollMode
 */
inline var ViewPager2.realOverScrollMode: Int
    get() {
        val vpChild = this.getChildAt(0)
        if (vpChild is RecyclerView) {
            return vpChild.overScrollMode
        }
        // should not reach here.
        return -1
    }
    set(value) {
        val vpChild = this.getChildAt(0)
        if (vpChild is RecyclerView) {
            vpChild.overScrollMode = value
        }
    }

@Deprecated("use BottomNavigationViewMediator instead.")
interface OnFragmentSelectedListener {
    fun onFragmentSelected(currentFragment: Fragment)
}