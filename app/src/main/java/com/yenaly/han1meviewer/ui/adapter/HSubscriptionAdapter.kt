package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.Subscription
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.util.showAlertDialog

class HSubscriptionAdapter : BaseDifferAdapter<Subscription, QuickViewHolder>(COMPARATOR) {

    companion object {
        const val DELETE = 1
        const val CHECK = 1 shl 1
    }

    private object COMPARATOR : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Subscription, newItem: Subscription): Int {
            var bitmap = 0
            if (oldItem.isDeleteVisible != newItem.isDeleteVisible) {
                bitmap = bitmap or DELETE
            }
            return bitmap
        }
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Subscription?) {
        item ?: return
        val context = this.context
        if (context is SearchActivity) {
            holder.getView<CheckBox>(R.id.cb_select).apply {
                isVisible = item.name == context.viewModel.subscriptionBrand
                isChecked = isVisible
            }
            holder.getView<View>(R.id.btn_delete).isVisible = item.isDeleteVisible
        }
        holder.setText(R.id.tv_artist, item.name)
        holder.getView<ImageView>(R.id.iv_artist).apply {
            load(item.avatarUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: Subscription?,
        payloads: List<Any>
    ) {
        item ?: return
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position, item)
            return
        }
        val payload = payloads.first() as Int
        if (payload and DELETE != 0) {
            holder.getView<View>(R.id.btn_delete).isVisible = item.isDeleteVisible
            holder.getView<View>(R.id.cb_select).isVisible = item.isCheckBoxVisible
        }
        if (payload and CHECK != 0) {
            val context = this.context
            if (context is SearchActivity) {
                holder.getView<CheckBox>(R.id.cb_select).apply {
                    isVisible = item.name == context.viewModel.subscriptionBrand
                    isChecked = isVisible
                }
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_h_subscription, parent).apply {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener
                if (item.isDeleteVisible) return@setOnClickListener
                getView<CheckBox>(R.id.cb_select).apply {
                    if (isChecked) {
                        isVisible = false
                        isChecked = false
                        if (context is SearchActivity) {
                            context.viewModel.subscriptionBrand = null
                            context.setSearchText(null)
                            notifyItemChanged(position, CHECK)
                        }
                    } else {
                        isVisible = true
                        isChecked = true
                        if (context is SearchActivity) {
                            context.viewModel.subscriptionBrand = item.name
                            context.setSearchText(item.name, canTextChange = false)
                            notifyItemRangeChanged(0, itemCount, CHECK)
                        }
                    }
                }
            }
            itemView.setOnLongClickListener {
                if (context is SearchActivity) {
                    val btnDelete = getView<View>(R.id.btn_delete)
                    if (btnDelete.isGone) {
                        submitList(items.map {
                            it.copy(
                                isDeleteVisible = true,
                                isCheckBoxVisible = false
                            )
                        })
                    } else {
                        submitList(items.map {
                            it.copy(
                                isDeleteVisible = false,
                                isCheckBoxVisible = it.name == context.viewModel.subscriptionBrand
                            )
                        })
                    }
                }
                true
            }
            getView<View>(R.id.btn_delete).setOnClickListener {
                val position = bindingAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener
                context.showAlertDialog {
                    setTitle(R.string.sure_to_delete)
                    setMessage(context.getString(R.string.sure_to_delete_s, item.name))
                    setPositiveButton(R.string.confirm) { _, _ ->
                        if (context is SearchActivity) {
                            context.myListViewModel.subscription.deleteSubscription(
                                item.artistId, position
                            )
                        }
                    }
                    setNegativeButton(R.string.cancel, null)
                }
            }
        }
    }
}