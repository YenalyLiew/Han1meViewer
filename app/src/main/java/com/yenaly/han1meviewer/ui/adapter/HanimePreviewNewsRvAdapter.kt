package com.yenaly.han1meviewer.ui.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemHanimePreviewNewsBinding
import com.yenaly.han1meviewer.logic.model.HanimePreviewModel
import com.yenaly.han1meviewer.ui.activity.PreviewActivity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.ui.popup.CoilImageLoader
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/25 025 15:26
 */
class HanimePreviewNewsRvAdapter :
    BaseQuickAdapter<HanimePreviewModel.PreviewInfo, HanimePreviewNewsRvAdapter.ViewHolder>(R.layout.item_hanime_preview_news) {

    private val imageLoader = CoilImageLoader()

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemHanimePreviewNewsBinding>(view) {
        val binding = dataBinding!!
    }

    override fun convert(holder: ViewHolder, item: HanimePreviewModel.PreviewInfo) {
        holder.binding.tvTitle.text = item.title
        holder.binding.tvIntroduction.setContent(item.introduction)
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvBrand.text = item.brand
        holder.binding.tvReleaseDate.text = item.releaseDate
        holder.binding.tvVideoTitle.text = item.videoTitle
        holder.binding.tags.setTags(item.tags)
        holder.binding.rvPreview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = PreviewPicRvAdapter(item)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.itemView.apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                if (context is PreviewActivity) {
                    (context as PreviewActivity).startActivity<VideoActivity>(VIDEO_CODE to item.videoCode)
                }
            }
        }
    }

    private inner class PreviewPicRvAdapter(private val item: HanimePreviewModel.PreviewInfo) :
        BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_hanime_preview_news_pic,
            item.relatedPicsUrl.toMutableList()
        ) {
        override fun convert(holder: BaseViewHolder, item: String) {
            holder.getView<ImageView>(R.id.iv_preview_news_pic).load(item) {
                crossfade(true)
            }
        }

        override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                XPopup.Builder(context).asImageViewer(
                    it as? ImageView,
                    position, item.relatedPicsUrl, { popupView, pos ->
                        popupView.updateSrcView(recyclerView.getChildAt(pos) as? ImageView)
                    }, imageLoader
                ).show()
            }
        }
    }
}