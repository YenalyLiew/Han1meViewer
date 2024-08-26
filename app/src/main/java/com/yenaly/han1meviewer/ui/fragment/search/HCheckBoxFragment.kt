package com.yenaly.han1meviewer.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.SearchOption
import com.yenaly.yenaly_libs.base.frame.FrameFragment
import com.yenaly.yenaly_libs.utils.dp


class HCheckBoxFragment(
    private val adapter: BaseQuickAdapter<SearchOption, *>,
    private val items: List<SearchOption>?,
    private val spanCount: Int = 3,
) : FrameFragment() {

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
        adapter.submitList(items)
    }
}