package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
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

    private val back: MaterialButton
    private val search: MaterialButton
    private val tag: MaterialButton
    private val historyRv: RecyclerView
    private val searchBar: TextInputEditText

    /**
     * 历史记录是否折叠
     */
    private var isCollapsed = true

    init {
        inflate(context, R.layout.layout_hanime_search_bar, this)
        back = findViewById(R.id.btn_back)
        search = findViewById(R.id.btn_search)
        tag = findViewById(R.id.btn_tag)
        historyRv = findViewById(R.id.rv_history)
        searchBar = findViewById(R.id.et_search)

        // init
        historyRv.layoutManager = LinearLayoutManager(context)
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClickListener?.let { listener ->
                    listener(searchBar, searchBar.text?.toString().orEmpty())
                    true
                } ?: false
            }
            false
        }
    }

    var adapter: BaseQuickAdapter<SearchHistoryEntity, out BaseViewHolder>? = null
        set(value) {
            field = value
            historyRv.adapter = value
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
                searchBar.hideIme(window)
                value?.invoke(it, searchBar.text?.toString().orEmpty())
            }
        }

    var onTagClickListener: ((View) -> Unit)? = null
        set(value) {
            field = value
            tag.setOnClickListener(value)
        }

    var searchText: String?
        get() = searchBar.text?.toString()
        set(value) {
            searchBar.setText(value)
            searchBar.setSelection(searchBar.length())
        }

    /**
     * 文字修改或者获得焦点的Flow
     */
    fun textChangeFlow() = callbackFlow {
        val watcher = searchBar.addTextChangedListener { text ->
            trySend(text?.toString())
            Log.d("HanimeSearchBar", "watcher: $text")
        }

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showHistory()
                trySend(searchText)
                Log.d("HanimeSearchBar", "focus: $searchText")
            } else {
                hideHistory()
            }
        }

        awaitClose {
            searchBar.removeTextChangedListener(watcher)
            searchBar.onFocusChangeListener = null
        }
    }

    // var history
    //     get() = adapter?.data ?: mutableListOf()
    //     set(value) {
    //         adapter?.setDiffNewData(value)
    //     }

    fun showHistory() {
        historyRv.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .alpha(1F)
            .withStartAction { historyRv.visibility = VISIBLE }
            .start()
        back.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .rotation(45F)
            .start()
        isCollapsed = false
    }

    fun hideHistory() {
        searchBar.hideIme(window)
        historyRv.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .alpha(0F)
            .withEndAction { historyRv.visibility = GONE }
            .start()
        back.animate()
            .setInterpolator(animInterpolator)
            .setDuration(animDuration)
            .rotation(0F)
            .start()
        isCollapsed = true
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && !isCollapsed) {
            hideHistory()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}