package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import coil.load
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.ui.popup.CoilImageLoader

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/29 029 20:11
 */
class HanimePreviewTourRvAdapter : BaseDifferAdapter<HanimeInfo, QuickViewHolder>(
    HanimeVideoRvAdapter.COMPARATOR
) {

    private val imageLoader = CoilImageLoader()

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: HanimeInfo?,
    ) {
        holder.getView<ImageView>(R.id.cover).load(item?.coverUrl) {
            crossfade(true)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_preview_tour_simplified, parent).also { viewHolder ->
            viewHolder.itemView.setOnLongClickListener {
                val position = viewHolder.bindingAdapterPosition
                val urlList = items.map { it.coverUrl }
                XPopup.Builder(context).asImageViewer(
                    viewHolder.getView(R.id.cover), position, urlList, { popupView, pos ->
                        popupView.updateSrcView(recyclerView.getChildAt(pos) as? ImageView)
                    }, imageLoader
                ).show()
                return@setOnLongClickListener true
            }
        }
    }
}