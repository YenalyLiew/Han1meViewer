package com.yenaly.han1meviewer.ui.view

import android.animation.LayoutTransition
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.view.hideIme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.parcelize.Parcelize

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
        const val ANIM_DURATION = 300L
    }

    private val window = checkNotNull(context.activity?.window)

    private val root = inflate(context, R.layout.layout_hanime_search_bar, this) as ViewGroup
    private val back: MaterialButton = findViewById(R.id.btn_back)
    private val search: MaterialButton = findViewById(R.id.btn_search)
    private val tag: MaterialButton = findViewById(R.id.btn_tag)
    private val rvHistory: RecyclerView = findViewById(R.id.rv_history)
    private val etSearch: TextInputEditText = findViewById(R.id.et_search)

    /**
     * 历史记录是否折叠
     */
    private var isCollapsed = true

    init {
        // init
        root.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
            setDuration(LayoutTransition.CHANGING, ANIM_DURATION)
            setInterpolator(LayoutTransition.CHANGING, animInterpolator)
        }
        rvHistory.layoutManager = LinearLayoutManager(context)

        // #issue-176: 禁用默认动画，涉及到许多崩溃，到现在官方也没解决问题，不得不牺牲体验
        // 根本问题：RecyclerView 的 itemAnimator 与 LayoutTransition 有冲突
        // 与其相关的网站链接：
        // https://github.com/google/flexbox-layout/issues/240
        // https://github.com/airbnb/epoxy/issues/689
        // https://stackoverflow.com/questions/30078834
        rvHistory.itemAnimator = null

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClickListener?.let { listener ->
                    listener(etSearch, etSearch.text?.toString().orEmpty())
                    true
                } == true
            }
            false
        }
    }

    var canTextChange: Boolean = true
        set(value) {
            field = value
            etSearch.isEnabled = value
        }

    var historyAdapter: BaseQuickAdapter<SearchHistoryEntity, out QuickViewHolder>? = null
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
                hideHistory()
            }
        }

        awaitClose {
            etSearch.removeTextChangedListener(watcher)
            etSearch.onFocusChangeListener = null
        }
    }

    fun showHistory() {
//        val slide = Slide(Gravity.BOTTOM).apply {
//            duration = animDuration
//            interpolator = animInterpolator
//            addTarget(rvHistory)
//        }
//        TransitionManager.beginDelayedTransition(searchBar, slide)

        rvHistory.visibility = VISIBLE
        back.animate()
            .setInterpolator(animInterpolator)
            .setDuration(ANIM_DURATION)
            .rotation(45F)
            .start()
        isCollapsed = false
    }

    fun hideHistory(): Boolean {
        if (isCollapsed) return false
        etSearch.hideIme(window)
        Log.d("HanimeSearchBar", "History Height: ${rvHistory.height}")
//        val slide = Slide(Gravity.TOP).apply {
//            duration = animDuration
//            interpolator = animInterpolator
//            addTarget(rvHistory)
//        }
//        TransitionManager.beginDelayedTransition(searchBar, slide)

        rvHistory.visibility = GONE
        back.animate()
            .setInterpolator(animInterpolator)
            .setDuration(ANIM_DURATION)
            .rotation(0F)
            .start()
        isCollapsed = true
        return true
    }

    // 使用 onBackPressedDispatcher.addCallback 替换
    // 这个方法在 API 34 以上（貌似）无法返回 key event
    //
    // override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    //     if (event.keyCode == KeyEvent.KEYCODE_BACK && !isCollapsed) {
    //         hideHistory()
    //         return true
    //     }
    //     return super.dispatchKeyEvent(event)
    // }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState(), isCollapsed)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        this.isCollapsed = state.isCollapsed
        if (!isCollapsed) {
            showHistory()
        }
    }

    @Parcelize
    data class SavedState(
        val ss: Parcelable?,
        val isCollapsed: Boolean
    ) : BaseSavedState(ss)
}