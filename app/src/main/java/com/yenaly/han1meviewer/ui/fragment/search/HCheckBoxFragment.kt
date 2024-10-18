package com.yenaly.han1meviewer.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.SearchOption
import com.yenaly.han1meviewer.ui.fragment.search.HMultiChoicesDialog.Companion.UNKNOWN_ADAPTER
import com.yenaly.yenaly_libs.base.frame.FrameFragment
import com.yenaly.yenaly_libs.utils.arguments
import com.yenaly.yenaly_libs.utils.dp
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.unsafeLazy

class HCheckBoxFragment : FrameFragment() {

    companion object {

        const val SCOPE_NAME_RES = "scope_name_res"
        const val ITEMS = "items"
        const val SPAN_COUNT = "span_count"

        const val DEF_SPAN_COUNT = 3

        fun newInstance(
            scopeNameRes: Int,
            items: List<SearchOption>,
            spanCount: Int = DEF_SPAN_COUNT,
        ) = HCheckBoxFragment().makeBundle(
            SCOPE_NAME_RES to scopeNameRes,
            ITEMS to items,
            SPAN_COUNT to spanCount
        )
    }

    val scopeNameRes by arguments(SCOPE_NAME_RES, UNKNOWN_ADAPTER)
    val items by arguments(ITEMS, emptyList<SearchOption>())
    val spanCount by arguments(SPAN_COUNT, DEF_SPAN_COUNT)

    val adapter by unsafeLazy {
        HMultiChoicesDialog.adapterMap?.get(scopeNameRes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rv = inflater.inflate(R.layout.layout_rv_scrollbars, container, false) as RecyclerView
        rv.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            isVerticalScrollBarEnabled = true

            setPadding(8.dp, 0, 8.dp, 0)

            layoutManager = GridLayoutManager(context, spanCount)
            adapter = this@HCheckBoxFragment.adapter
        }
        return rv
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this@HCheckBoxFragment.adapter?.submitList(items)
    }
}