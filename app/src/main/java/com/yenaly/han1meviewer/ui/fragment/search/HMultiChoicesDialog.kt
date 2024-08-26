package com.yenaly.han1meviewer.ui.fragment.search

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.util.size
import androidx.core.util.valueIterator
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.SearchOption
import com.yenaly.han1meviewer.ui.adapter.HSearchTagAdapter
import com.yenaly.han1meviewer.util.createAlertDialog
import com.yenaly.yenaly_libs.utils.findActivity
import com.yenaly.yenaly_libs.utils.view.SimpleFragmentStateAdapter
import com.yenaly.yenaly_libs.utils.view.attach

class HMultiChoicesDialog(
    val context: Context,
    @StringRes private val titleRes: Int,
    private val hasSingleItem: Boolean = false
) {

    companion object {
        const val UNKNOWN_ADAPTER = -1
    }

    private val pageAdapter = SimpleFragmentStateAdapter(context.findActivity())

    private val coreView = View.inflate(context, R.layout.pop_up_hanime_search_tag, null)
    private val tab = coreView.findViewById<TabLayout>(R.id.tl_tag)
    private val page = coreView.findViewById<ViewPager2>(R.id.vp_tag)

    private val adapterMap: SparseArray<HSearchTagAdapter> = SparseArray()
    private val nameResList = mutableListOf<Int>()

    private var onSave: ((AlertDialog) -> Unit)? = null
    private var onReset: ((AlertDialog) -> Unit)? = null

    private var isAdded = false

    private val dialog = context.createAlertDialog {
        setTitle(titleRes)
        setPositiveButton(R.string.save, null)
        setNeutralButton(R.string.reset, null)
        setView(coreView)
    }

    init {
        page.adapter = pageAdapter
        page.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

        dialog.setOnShowListener { di ->
            page.requestLayout()
            val ad = di as AlertDialog
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                onSave?.invoke(ad)
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                onReset?.invoke(ad)
            }
        }
    }

    private fun getTagScope(@StringRes scopeNameRes: Int): HSearchTagAdapter? =
        adapterMap[scopeNameRes]

    fun addTagScope(@StringRes scopeNameRes: Int?, items: List<SearchOption>?, spanCount: Int = 3) {
        if (items.isNullOrEmpty() || isAdded) return
        if (hasSingleItem) {
            tab.isVisible = false
            page.isUserInputEnabled = false
            isAdded = true
        }
        val tagAdapter = HSearchTagAdapter()
        adapterMap[scopeNameRes ?: UNKNOWN_ADAPTER] = tagAdapter
        nameResList += scopeNameRes ?: UNKNOWN_ADAPTER
        pageAdapter.addFragment { HCheckBoxFragment(tagAdapter, items, spanCount) }
    }

    fun setOnSaveListener(action: (AlertDialog) -> Unit) {
        onSave = action
    }

    fun setOnResetListener(action: (AlertDialog) -> Unit) {
        onReset = action
    }

    fun loadSavedTags(saved: SparseArray<Set<SearchOption>>) {
        for (i in 0..<saved.size) {
            val key = saved.keyAt(i)
            val value = saved.valueAt(i)
            getTagScope(key)?.let { adapter ->
                adapter.checkedSet.clear()
                adapter.checkedSet += value
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAllChecks() {
        adapterMap.valueIterator().forEach { adapter ->
            if (adapter.checkedSet.isNotEmpty()) {
                adapter.checkedSet.clear()
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun collectCheckedTags(): SparseArray<Set<SearchOption>> {
        return SparseArray<Set<SearchOption>>().apply {
            for (i in 0..<adapterMap.size) {
                val key = adapterMap.keyAt(i)
                val value = adapterMap.valueAt(i)
                if (value.checkedSet.isNotEmpty()) {
                    this[key] = value.checkedSet
                }
            }
        }
    }

    fun show() {
        if (!hasSingleItem) {
            tab.attach(page) { tab, pos ->
                tab.setText(nameResList[pos])
            }
        }
        dialog.show()
    }
}