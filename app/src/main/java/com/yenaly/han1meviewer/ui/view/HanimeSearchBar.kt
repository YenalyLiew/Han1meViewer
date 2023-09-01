package com.yenaly.han1meviewer.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.view.hideIme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * 搜索界面的搜索栏
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/11 011 15:20
 */
class HanimeSearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    companion object {
        val animInterpolator = FastOutSlowInInterpolator()
        const val animDuration = 300L
    }

    private val window = checkNotNull(context.activity?.window)

    private val searchBar: ViewGroup
    private val back: MaterialButton
    private val search: MaterialButton
    private val tag: MaterialButton
    private val rvHistory: RecyclerView
    private val etSearch: TextInputEditText

    /**
     * 历史记录是否折叠
     */
    private var isCollapsed = true

    init {
        inflate(context, R.layout.layout_hanime_search_bar, this)
        searchBar = findViewById(R.id.search_bar)
        back = findViewById(R.id.btn_back)
        search = findViewById(R.id.btn_search)
        tag = findViewById(R.id.btn_tag)
        rvHistory = findViewById(R.id.rv_history)
        etSearch = findViewById(R.id.et_search)

        // init
        rvHistory.layoutManager = LinearLayoutManager(context)
        rvHistory.itemAnimator?.removeDuration = 0
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClickListener?.let { listener ->
                    listener(etSearch, etSearch.text?.toString().orEmpty())
                    true
                } ?: false
            }
            false
        }
    }

    var adapter: BaseQuickAdapter<SearchHistoryEntity, out BaseViewHolder>? = null
        set(value) {
            field = value
            rvHistory.adapter = value
        }


    var onBackClickListener: ((View) -> Unit)? = null
        set(value) {
            field = value
            back.setOnClickListener {
                if (isCollapsed) {
                    value?.invoke(it)
                } else {
                    hideHistory()
                }
            }
        }

    var onSearchClickListener: ((View, String) -> Unit)? = null
        set(value) {
            field = value
            search.setOnClickListener {
                etSearch.hideIme(window)
                value?.invoke(it, etSearch.text?.toString().orEmpty())
            }
        }

    var onTagClickListener: ((View) -> Unit)? = null
        set(value) {
            field = value
            tag.setOnClickListener(value)
        }

    var searchText: String?
        get() = etSearch.text?.toString()
        set(value) {
            etSearch.setText(value)
            etSearch.setSelection(etSearch.length())
        }

    /**
     * 文字修改或者获得焦点的Flow
     */
    fun textChangeFlow() = callbackFlow {
        val watcher = etSearch.addTextChangedListener { text ->
            trySend(text?.toString())
            Log.d("HanimeSearchBar", "watcher: $text")
        }

        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showHistory()
                trySend(searchText)
                Log.d("HanimeSearchBar", "focus: $searchText")
            } else {
                if (!isCollapsed) hideHistory()
            }
        }

        awaitClose {
            etSearch.removeTextChangedListener(watcher)
            etSearch.onFocusChangeListener = null
        }
    }

    var history
        get() = adapter?.data ?: mutableListOf()
        set(value) {
            adapter?.also {
                it.setDiffNewData(value)
            }
        }

    fun showHistory() {
        rvHistory.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .alpha(1F)
            .withStartAction { rvHistory.visibility = VISIBLE }
            .start()

        back.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .rotation(45F)
            .start()
        isCollapsed = false
    }

    fun hideHistory() {
        if (!isCollapsed) {
            etSearch.hideIme(window)
            Log.d("HanimeSearchBar", "History Height: ${rvHistory.height}")
            rvHistory.visibility = GONE
            back.animate()
                .setInterpolator(animInterpolator)
                .setDuration(animDuration)
                .rotation(0F)
                .start()
            isCollapsed = true
        }
    }

    private fun View.buildHeightAnimation(
        from: Int, to: Int,
    ): ValueAnimator? {
        if (from == to) return null
        return ValueAnimator.ofInt(from, to).apply {
            duration = animDuration
            interpolator = animInterpolator
            addUpdateListener {
                val value = it.animatedValue as Int
                updateLayoutParams {
                    height = value
                }
            }
        }
    }

    private fun View.calcHeight(): Int {
        val matchParentMeasureSpec =
            MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec =
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        return measuredHeight
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && !isCollapsed) {
            hideHistory()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}