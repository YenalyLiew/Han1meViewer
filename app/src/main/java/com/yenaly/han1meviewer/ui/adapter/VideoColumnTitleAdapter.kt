package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.ui.activity.MainActivity
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/27 027 12:55
 */
class VideoColumnTitleAdapter : BaseSingleItemAdapter<Unit, QuickViewHolder> {

    private val notifyWhenSet: Boolean

    var title: String = EMPTY_STRING
        set(value) = if (notifyWhenSet) {
            field = value
            notifyItemChanged(0)
        } else field = value

    var subtitle: String? = null
        set(value) = if (notifyWhenSet) {
            field = value
            notifyItemChanged(0)
        } else field = value

    var onMoreHanimeListener: ((View) -> Unit)? = null

    constructor() : super() {
        notifyWhenSet = true
        title = EMPTY_STRING
        subtitle = null
    }

    constructor(title: String, subtitle: String? = null, notifyWhenSet: Boolean = false) : super() {
        this.title = title
        this.subtitle = subtitle
        this.notifyWhenSet = notifyWhenSet
    }

    constructor(
        @StringRes title: Int,
        @StringRes subtitle: Int = 0,
        notifyWhenSet: Boolean = false,
    ) : this(
        applicationContext.getString(title),
        if (subtitle != 0) applicationContext.getString(subtitle) else null,
        notifyWhenSet
    )

    override fun onBindViewHolder(holder: QuickViewHolder, item: Unit?) {
        holder.setGone(R.id.sub_title, subtitle == null)
        holder.setText(R.id.title, title)
        holder.setText(R.id.sub_title, subtitle)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_video_column_title, parent).also { viewHolder ->
            viewHolder.setGone(R.id.more, context !is MainActivity)
            viewHolder.getView<Button>(R.id.more).setOnClickListener(onMoreHanimeListener)
        }
    }
}