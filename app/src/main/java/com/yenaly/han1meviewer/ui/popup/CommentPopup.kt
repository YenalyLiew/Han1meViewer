package com.yenaly.han1meviewer.ui.popup

import android.content.Context
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.lxj.xpopup.core.BottomPopupView
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.unsafeLazy
import com.yenaly.yenaly_libs.utils.view.textString

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/20 020 09:48
 */
class CommentPopup(context: Context) : BottomPopupView(context) {

    private val editText by unsafeLazy { findViewById<EditText>(R.id.et_comment) }
    private val btnSend by unsafeLazy { findViewById<MaterialButton>(R.id.btn_send) }

    private var commentPrefix: String? = null
    private var sendListener: OnClickListener? = null

    override fun getImplLayoutId() = R.layout.pop_up_comment

    override fun onCreate() {
        super.onCreate()
        editText.hint = hint
        commentPrefix?.let(editText::append)
        sendListener?.let(btnSend::setOnClickListener)
    }

    /**
     * 得到你输入的内容
     */
    val comment get() = editText.textString()

    /**
     * 设置提示
     */
    var hint: CharSequence? = null

    /**
     * 设置前缀，用于回复子评论
     *
     * 例如：@xxx something
     */
    fun initCommentPrefix(username: String) {
        commentPrefix = "@$username "
    }

    /**
     * 设置发送按钮监听器
     */
    fun setOnSendListener(listener: OnClickListener) {
        this.sendListener = listener
    }
}