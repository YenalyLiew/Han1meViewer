package com.yenaly.han1meviewer.ui.adapter

import android.text.method.LinkMovementMethod
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.button.MaterialButton
import com.itxca.spannablex.spannable
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.ui.activity.SettingsActivity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.copyToClipboard
import com.yenaly.yenaly_libs.utils.encodeToStringByBase64
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/14 014 11:21
 */
class HKeyframesRvAdapter :
    BaseQuickAdapter<HKeyframeEntity, BaseViewHolder>(R.layout.item_h_keyframes) {

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HKeyframeEntity>() {
            override fun areItemsTheSame(
                oldItem: HKeyframeEntity,
                newItem: HKeyframeEntity,
            ) = oldItem.videoCode == newItem.videoCode

            override fun areContentsTheSame(
                oldItem: HKeyframeEntity,
                newItem: HKeyframeEntity,
            ) = oldItem == newItem
        }

        private val editResArray = intArrayOf(R.string.edit, R.string.delete, R.string.share)
        private val editResIconArray = intArrayOf(
            R.drawable.baseline_edit_24,
            R.drawable.ic_baseline_delete_24,
            R.drawable.ic_baseline_share_24
        )
    }

    override fun convert(holder: BaseViewHolder, item: HKeyframeEntity) {
        holder.setText(R.id.tv_title, item.title)
        holder.setGone(R.id.btn_edit, item.author != null)
        holder.getView<TextView>(R.id.tv_video_code).apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = spannable {
                context.getString(R.string.h_keyframe_title_prefix).text()
                item.videoCode.span {
                    clickable(color = context.getColor(R.color.video_code_link_text_color)) { _, videoCode ->
                        context.activity?.startActivity<VideoActivity>(VIDEO_CODE to videoCode)
                    }
                    underline()
                }
            }
        }
        holder.getView<RecyclerView>(R.id.rv_h_keyframe).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HKeyframeRvAdapter(item.videoCode, item).apply {
                isLocal = item.author == null
            }
        }
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        viewHolder.getView<ImageButton>(R.id.btn_edit).apply {
            setOnClickListener { view ->
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                XPopup.Builder(view.context).atView(view).isDarkTheme(true).asAttachList(
                    Array(editResArray.size) { idx -> view.context.getString(editResArray[idx]) },
                    editResIconArray
                ) { pos, _ ->
                    when (pos) {
                        0 -> modify(item) // 修改

                        1 -> delete(item) // 刪除

                        2 -> share(item)  // 分享
                    }
                }.show()
            }
        }
    }

    private fun share(item: HKeyframeEntity) {
        val toJson = Json.encodeToString(item)
        val toBase64 = toJson.encodeToStringByBase64(flag = Base64.NO_WRAP)
        val toContent = buildString {
            append(">>>")
            append(toBase64)
            append("<<<")
        }
        context.showAlertDialog {
            setTitle(R.string.share_to_other)
            setMessage("複製以下内容，分享給其他人，可以透過頂部右側添加按鈕來將其存入設備：\n$toContent")
            setPositiveButton(R.string.copy) { _, _ ->
                toContent.copyToClipboard()
                showShortToast(R.string.copy_to_clipboard)
            }
            setNegativeButton(R.string.cancel, null)
        }
    }

    private fun delete(item: HKeyframeEntity) {
        val activity = context
        if (activity is SettingsActivity) {
            activity.showAlertDialog {
                setTitle(R.string.sure_to_delete)
                setMessage(item.title)
                setPositiveButton(R.string.confirm) { _, _ ->
                    activity.viewModel.deleteHKeyframes(item)
                }
                setNegativeButton(R.string.cancel, null)
            }
        }
    }

    private fun modify(item: HKeyframeEntity) {
        val activity = context
        if (activity is SettingsActivity) {
            val view = View.inflate(activity, R.layout.dialog_modify_h_keyframes, null)
            val etTitle = view.findViewById<TextView>(R.id.et_title)
            val etVideoCode = view.findViewById<TextView>(R.id.et_video_code)
            etTitle.text = item.title
            etVideoCode.text = item.videoCode
            activity.showAlertDialog {
                setTitle(R.string.modify_h_keyframe)
                setView(view)
                setPositiveButton(R.string.confirm) { _, _ ->
                    val title = etTitle.text.toString()
                    activity.viewModel.updateHKeyframes(item.copy(title = title))
                }
                setNegativeButton(R.string.cancel, null)
            }
        }
    }
}

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/14 014 11:21
 */
class HKeyframeRvAdapter(
    private val videoCode: String,
    keyframe: HKeyframeEntity? = null,
) : BaseQuickAdapter<HKeyframeEntity.Keyframe, BaseViewHolder>(
    R.layout.item_h_keyframe, keyframe?.keyframes
) {

    /**
     * 是否是本地关键帧
     *
     * @return false if is shared, true otherwise.
     */
    var isLocal: Boolean = true

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HKeyframeEntity.Keyframe>() {
            override fun areItemsTheSame(
                oldItem: HKeyframeEntity.Keyframe,
                newItem: HKeyframeEntity.Keyframe,
            ) = oldItem.position == newItem.position

            override fun areContentsTheSame(
                oldItem: HKeyframeEntity.Keyframe,
                newItem: HKeyframeEntity.Keyframe,
            ) = oldItem == newItem
        }
    }

    override fun convert(holder: BaseViewHolder, item: HKeyframeEntity.Keyframe) {
        holder.setText(R.id.tv_keyframe, JZUtils.stringForTime(item.position))
        holder.setText(R.id.tv_index, "#${holder.bindingAdapterPosition + 1}")

        holder.setGone(R.id.btn_delete, !isLocal)
        holder.setGone(R.id.btn_edit, !isLocal)

        if (!item.prompt.isNullOrBlank()) {
            holder.setGone(R.id.tv_prompt, false)
            holder.setText(R.id.tv_prompt, "➥ " + item.prompt)
        } else {
            holder.setGone(R.id.tv_prompt, true)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        val context = this.context
        viewHolder.getView<MaterialButton>(R.id.btn_edit).apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)

                val view = View.inflate(context, R.layout.dialog_modify_h_keyframe, null)
                val etPrompt = view.findViewById<TextView>(R.id.et_prompt)
                val etPosition = view.findViewById<TextView>(R.id.et_position)
                etPrompt.text = item.prompt
                etPosition.text = item.position.toString()

                context.showAlertDialog {
                    setTitle(R.string.modify_h_keyframe)
                    setView(view)
                    setPositiveButton(R.string.confirm) { _, _ ->
                        val prompt = etPrompt.text.toString()
                        val pos = etPosition.text.toString().toLong()
                        when (context) {
                            is SettingsActivity -> {
                                context.viewModel.modifyHKeyframe(
                                    videoCode, item, HKeyframeEntity.Keyframe(
                                        position = pos,
                                        prompt = prompt
                                    )
                                )
                                showShortToast("修改成功")
                            }

                            is VideoActivity -> {
                                context.viewModel.modifyHKeyframe(
                                    videoCode, item, HKeyframeEntity.Keyframe(
                                        position = pos,
                                        prompt = prompt
                                    )
                                )
                                // showShortToast("修改成功") // 這裏不用提示，因為 VideoActivity 有 Flow 操控
                            }
                        }
                    }
                    setNegativeButton(R.string.cancel, null)
                }
            }
        }
        viewHolder.getView<MaterialButton>(R.id.btn_delete).apply {
            setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                val item = getItem(position)
                it.context.showAlertDialog {
                    setTitle(R.string.sure_to_delete)
                    setMessage(JZUtils.stringForTime(item.position))
                    setPositiveButton(R.string.confirm) { _, _ ->
                        when (context) {
                            is SettingsActivity -> {
                                context.viewModel.removeHKeyframe(videoCode, item)
                                showShortToast("刪除成功")
                            }

                            is VideoActivity -> {
                                context.viewModel.removeHKeyframe(videoCode, item)
                                // showShortToast("刪除成功") // 這裏不用提示，因為 VideoActivity 有 Flow 操控
                            }
                        }
                    }
                    setNegativeButton(R.string.cancel, null)
                }
            }
        }
    }
}