package com.yenaly.han1meviewer.ui.adapter

import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.PlaylistsModel
import com.yenaly.han1meviewer.ui.fragment.home.MyPlaylistFragment

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/28 028 12:22
 */
class PlaylistRvAdapter(private val fragment: Fragment) :
    BaseQuickAdapter<PlaylistsModel.Playlist, BaseViewHolder>(R.layout.item_playlist) {

    override fun convert(holder: BaseViewHolder, item: PlaylistsModel.Playlist) {
        holder.setText(R.id.tv_title, item.title)
        holder.setText(R.id.tv_count, item.total.toString())
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        check(fragment is MyPlaylistFragment)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            fragment.listCode = item.listCode
            fragment.listTitle = item.title
            fragment.getNewPlaylistItems()
            fragment.binding.dlPlaylist.closeDrawers()
        }
    }
}