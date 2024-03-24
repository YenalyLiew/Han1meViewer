package com.yenaly.han1meviewer.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.util.showAlertDialog

/**
 * 用于播放清單的標題和介紹
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/30 030 00:28
 */
class PlaylistHeader @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val tvTitle: TextView
    private val btnDelete: Button
    private val tvDesc: TextView
    private val btnEdit: Button

    init {
        inflate(context, R.layout.layout_playlist_header_v2, this)
        tvTitle = findViewById(R.id.tv_title)
        btnDelete = findViewById(R.id.btn_delete)
        tvDesc = findViewById(R.id.tv_desc)
        btnEdit = findViewById(R.id.btn_edit)
        init()
    }

    var title: String? = null
        set(value) {
            field = value
            tvTitle.text = value
        }

    var description: String? = null
        set(value) {
            field = value
            tvDesc.text =
                if (!value.isNullOrEmpty()) value else context.getString(R.string.no_description)
        }

    var onChangedListener: ((title: String, desc: String) -> Unit)? = null

    var onDeleteActionListener: (() -> Unit)? = null

    @SuppressLint("InflateParams")
    private fun init() {
        btnDelete.setOnClickListener {
            context.showAlertDialog {
                setTitle(R.string.delete_the_playlist)
                setMessage(R.string.sure_to_delete)
                setPositiveButton(R.string.confirm) { _, _ ->
                    onDeleteActionListener?.invoke()
                }
                setNegativeButton(R.string.cancel, null)
            }
        }
        btnEdit.setOnClickListener {
            context.showAlertDialog {
                setTitle(R.string.modify_title_or_desc)
                val etView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.dialog_playlist_modify_edit_text, null)
                val etTitle = etView.findViewById<EditText>(R.id.et_title)
                val etDesc = etView.findViewById<EditText>(R.id.et_desc)
                etTitle.setText(title)
                etDesc.setText(description)
                setView(etView)
                setPositiveButton(R.string.confirm) { _, _ ->
                    onChangedListener?.invoke(
                        etTitle.text.toString(),
                        etDesc.text.toString()
                    )
                }
                setNegativeButton(R.string.cancel, null)
            }
        }
    }
}