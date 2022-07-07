package com.yenaly.han1meviewer.ui.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/16 016 16:19
 */
class CodeSelectedChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Chip(context, attrs, defStyleAttr) {

    init {
        // must do check by programmatically.
        this.isCheckable = false
    }

    override fun setCheckable(checkable: Boolean) {
        super.setCheckable(false)
    }

    @Suppress("unchecked_cast")
    override fun setChecked(checked: Boolean) {
        val drawableField = javaClass.superclass.getDeclaredField("chipDrawable")
        drawableField.isAccessible = true
        val drawable = drawableField.get(this) as? ChipDrawable
        drawable?.isCheckable = true
        super.setChecked(checked)
        drawable?.isCheckable = false
    }
}