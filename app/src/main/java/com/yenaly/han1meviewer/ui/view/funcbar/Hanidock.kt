package com.yenaly.han1meviewer.ui.view.funcbar

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yenaly.han1meviewer.R

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @since 2025/3/11 22:02
 */
class Hanidock @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val hanidapter = Hanidapter()

    init {
        inflate(context, R.layout.layout_hanidock, this)
        recyclerView = findViewById(R.id.rv_func)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = hanidapter
    }

    var hanidokitems: List<Hanidokitem>
        get() = hanidapter.hanidontroller.currentHanidokitems
        set(value) {
            hanidapter.hanidontroller.initialize(value)
            hanidapter.items = value
        }
}