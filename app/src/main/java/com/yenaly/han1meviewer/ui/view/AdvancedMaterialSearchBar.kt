package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.util.AttributeSet
import com.mancj.materialsearchbar.MaterialSearchBar

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/09/21 021 09:10
 */
class AdvancedMaterialSearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialSearchBar(context, attrs) {

    private var textListener: ((String?) -> Unit)? = null

    fun appendText(text: String?) {
        text?.let(searchEditText::append)
    }

    fun appendTextWithListener(text: String?) {
        text?.let(searchEditText::append)
        textListener?.invoke(text)
    }

    fun setTextWithListener(text: String?) {
        searchEditText.setText(text)
        textListener?.invoke(text)
    }

    fun setTextListener(listener: (String?) -> Unit) {
        this.textListener = listener
    }
}