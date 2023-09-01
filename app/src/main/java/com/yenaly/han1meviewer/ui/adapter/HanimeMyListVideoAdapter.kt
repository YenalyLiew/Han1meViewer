package com.yenaly.han1meviewer.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.logic.model.HanimeInfoModel
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/29 029 20:10
 */
class HanimeMyListVideoAdapter :
    BaseQuickAdapter<HanimeInfoModel, BaseViewHolder>(R.layout.item_hanime_video_simplified) {

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HanimeInfoModel>() {
            override fun areItemsTheSame(
                oldItem: HanimeInfoModel,
                newItem: HanimeInfoModel,
            ): Boolean {
                return oldItem.videoCode == newItem.videoCode
            }

            override fun areContentsTheSame(
                oldItem: HanimeInfoModel,
                newItem: HanimeInfoModel,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun convert(holder: BaseViewHolder, item: HanimeInfoModel) {
        holder.getView<TextView>(R.id.title).text = item.title
        holder.getView<ImageView>(R.id.cover).load(item.coverUrl) {
            crossfade(true)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        viewHolder.getView<View>(R.id.linear).layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        viewHolder.getView<ImageView>(R.id.cover).scaleType = ImageView.ScaleType.CENTER_CROP
        viewHolder.itemView.apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                val videoCode = item.videoCode
                context.activity?.startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
            // setOnLongClickListener 由各自的 Fragment 实现
        }
    }
}